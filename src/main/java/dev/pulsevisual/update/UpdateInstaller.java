package dev.pulsevisual.update;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Standalone JDK-only helper: its class is copied outside the mod jar before launch. */
public final class UpdateInstaller {
    public static String hash(Path file) throws Exception {
        var digest=MessageDigest.getInstance("SHA-256");
        try(var in=Files.newInputStream(file)){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)digest.update(buffer,0,n);}
        return HexFormat.of().formatHex(digest.digest());
    }
    public static void install(Path target,Path staged,String oldHash,String newHash) throws Exception {
        target=target.toAbsolutePath().normalize();staged=staged.toAbsolutePath().normalize();
        if(Files.isSymbolicLink(target)||Files.isSymbolicLink(staged)||!target.getFileName().toString().endsWith(".jar"))throw new IllegalArgumentException("Invalid update target");
        if(!hash(target).equals(oldHash)||!hash(staged).equals(newHash))throw new IllegalStateException("File changed; update cancelled");
        Path backup=target.resolveSibling(target.getFileName()+".previous"),incoming=target.resolveSibling(target.getFileName()+".incoming");
        Files.copy(target,backup,StandardCopyOption.REPLACE_EXISTING);
        Files.copy(staged,incoming,StandardCopyOption.REPLACE_EXISTING);
        if(!hash(incoming).equals(newHash))throw new IllegalStateException("Copy verification failed");
        try{Files.move(incoming,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        catch(AtomicMoveNotSupportedException e){Files.move(incoming,target,StandardCopyOption.REPLACE_EXISTING);}
        Files.deleteIfExists(staged);
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=5)return;
        var process=ProcessHandle.of(Long.parseLong(args[0]));
        if(process.isPresent())process.get().onExit().join();
        // Windows may release jar handles slightly after process termination.
        for(int attempt=0;attempt<30;attempt++){
            try{install(Path.of(args[1]),Path.of(args[2]),args[3],args[4]);return;}
            catch(java.io.IOException e){if(attempt==29)throw e;Thread.sleep(1000);}
        }
    }
}
