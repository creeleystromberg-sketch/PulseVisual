package dev.pulsevisual;

import dev.pulsevisual.update.AutoUpdater;
import dev.pulsevisual.update.UpdateInstaller;
import java.nio.file.*;
import java.util.zip.*;
import java.nio.charset.StandardCharsets;

public final class UpdateRegressionTest {
    private static int checks;
    private static void check(boolean value){if(!value)throw new AssertionError("update regression "+checks);checks++;}
    private static void jar(Path path,String id,String version) throws Exception {
        try(var zip=new ZipOutputStream(Files.newOutputStream(path))){zip.putNextEntry(new ZipEntry("fabric.mod.json"));zip.write(("{\"id\":\""+id+"\",\"version\":\""+version+"\",\"depends\":{\"minecraft\":\"=1.21.11\"}}").getBytes(StandardCharsets.UTF_8));zip.closeEntry();}
    }
    public static void main(String[] args) throws Exception {
        check(AutoUpdater.newer("2.2.0","2.1.0"));check(!AutoUpdater.newer("2.1.0","2.2.0"));check(!AutoUpdater.newer("2.2.0","2.2.0"));check(!AutoUpdater.newer("../../bad","2.2.0"));
        Path dir=Files.createTempDirectory("pulsevisual-update-test-"),target=dir.resolve("installed.jar"),staged=dir.resolve("new.jar");
        try{
            jar(target,"pulsevisual","2.1.0");jar(staged,"pulsevisual","2.2.0");
            String before=UpdateInstaller.hash(target),after=UpdateInstaller.hash(staged);
            AutoUpdater.validateJar(staged,"2.2.0");checks++;
            boolean rejected=false;try{UpdateInstaller.install(target,staged,before,"0".repeat(64));}catch(IllegalStateException expected){rejected=true;}check(rejected);check(UpdateInstaller.hash(target).equals(before));
            UpdateInstaller.install(target,staged,before,after);check(UpdateInstaller.hash(target).equals(after));check(UpdateInstaller.hash(dir.resolve("installed.jar.previous")).equals(before));check(!Files.exists(staged));
            jar(staged,"othermod","2.2.0");rejected=false;try{AutoUpdater.validateJar(staged,"2.2.0");}catch(IllegalArgumentException expected){rejected=true;}check(rejected);
            jar(staged,"pulsevisual","2.1.0");rejected=false;try{AutoUpdater.validateJar(staged,"2.2.0");}catch(IllegalArgumentException expected){rejected=true;}check(rejected);
        }finally{try(var entries=Files.list(dir)){for(Path p:entries.toList())Files.deleteIfExists(p);}Files.delete(dir);}
        System.out.println("PASS: "+checks+" updater checks (version, hash, identity, replacement, backup)");
    }
}
