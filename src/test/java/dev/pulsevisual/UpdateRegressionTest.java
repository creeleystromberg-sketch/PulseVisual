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
        if(args.length==1){
            var download=AutoUpdater.class.getDeclaredMethod("download",java.net.http.HttpClient.class,String.class,int.class);download.setAccessible(true);
            try(var http=java.net.http.HttpClient.newBuilder().followRedirects(java.net.http.HttpClient.Redirect.NORMAL).build()){
                String base="https://github.com/"+AutoUpdater.REPOSITORY+"/releases/";
                byte[] json=(byte[])download.invoke(null,http,base+"latest/download/pulsevisual-update.json",65536);
                var manifest=com.google.gson.JsonParser.parseString(new String(json,StandardCharsets.UTF_8)).getAsJsonObject();
                String version=manifest.get("version").getAsString();check(version.equals(args[0]));check(AutoUpdater.newer(version,"2.1.0"));
                Path actual=Files.createTempFile("pulsevisual-published-",".jar");
                try{
                    Files.write(actual,(byte[])download.invoke(null,http,base+"download/v"+version+"/pulsevisual-"+version+".jar",16*1024*1024));
                    check(UpdateInstaller.hash(actual).equals(manifest.get("sha256").getAsString()));AutoUpdater.validateJar(actual,version);
                    System.out.println("PASS: published GitHub manifest, download, checksum and mod identity "+version);
                }finally{Files.deleteIfExists(actual);}
            }
        }
    }
}
