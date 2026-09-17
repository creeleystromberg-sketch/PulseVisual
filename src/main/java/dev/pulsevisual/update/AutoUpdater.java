package dev.pulsevisual.update;

import com.google.gson.JsonParser;
import dev.pulsevisual.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.zip.ZipFile;

/** Downloads only releases from this project's pinned GitHub repository. */
public final class AutoUpdater {
    public static final String REPOSITORY="creeleystromberg-sketch/PulseVisual";
    private static final String BASE="https://github.com/"+REPOSITORY+"/releases/";
    private static final ScheduledExecutorService WORKER=Executors.newSingleThreadScheduledExecutor(r->{var t=new Thread(r,"PulseVisual updates");t.setDaemon(true);return t;});
    private static volatile String status="Not checked";
    private static volatile Pending pending;
    private record Pending(Path target,Path jar,Path helper,String oldHash,String hash){}
    private AutoUpdater(){}
    public static String status(){return status;}
    public static void start(){
        if(FabricLoader.getInstance().isDevelopmentEnvironment()){status="Disabled in development";return;}
        WORKER.scheduleWithFixedDelay(()->{if(ConfigManager.get().automaticUpdates)check();},10,900,TimeUnit.SECONDS);
    }
    public static void checkNow(){WORKER.execute(AutoUpdater::check);}
    public static boolean newer(String candidate,String current){
        if(!candidate.matches("\\d+\\.\\d+\\.\\d+")||!current.matches("\\d+\\.\\d+\\.\\d+"))return false;
        var a=candidate.split("\\.");var b=current.split("\\.");
        for(int i=0;i<3;i++){int c=new java.math.BigInteger(a[i]).compareTo(new java.math.BigInteger(b[i]));if(c!=0)return c>0;}return false;
    }
    private static byte[] download(HttpClient http,String url,int limit) throws Exception {
        var request=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(45)).header("User-Agent","PulseVisual-Updater").GET().build();
        var response=http.send(request,HttpResponse.BodyHandlers.ofInputStream());
        try(var in=response.body()){
            CompletableFuture.delayedExecutor(45,TimeUnit.SECONDS).execute(()->{try{in.close();}catch(java.io.IOException ignored){}});
            if(response.statusCode()!=200)throw new java.io.IOException("GitHub HTTP "+response.statusCode());
            var result=in.readNBytes(limit+1);if(result.length>limit)throw new java.io.IOException("Release too large");return result;
        }
    }
    public static void validateJar(Path jar,String version) throws Exception {
        try(var zip=new ZipFile(jar.toFile())){
            var entry=zip.getEntry("fabric.mod.json");if(entry==null)throw new IllegalArgumentException("Not a Fabric mod");
            try(var in=zip.getInputStream(entry)){
                byte[] bytes=in.readNBytes(65537);if(bytes.length>65536)throw new IllegalArgumentException("Invalid metadata");
                var meta=JsonParser.parseString(new String(bytes,StandardCharsets.UTF_8)).getAsJsonObject();
                if(!"pulsevisual".equals(meta.get("id").getAsString())||!version.equals(meta.get("version").getAsString())
                        ||!"=1.21.11".equals(meta.getAsJsonObject("depends").get("minecraft").getAsString()))throw new IllegalArgumentException("Wrong mod or game version");
            }
        }
    }
    private static void check(){
        if(FabricLoader.getInstance().isDevelopmentEnvironment()||pending!=null)return;
        try(var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build()){
            status="Checking GitHub";
            var mod=FabricLoader.getInstance().getModContainer("pulsevisual").orElseThrow();
            String current=mod.getMetadata().getVersion().getFriendlyString();
            var manifest=JsonParser.parseString(new String(download(http,BASE+"latest/download/pulsevisual-update.json",65536),StandardCharsets.UTF_8)).getAsJsonObject();
            String version=manifest.get("version").getAsString();
            if(!newer(version,current)){status="Up to date: "+current;return;}
            if(!"1.21.11".equals(manifest.get("minecraft").getAsString())||!"fabric".equals(manifest.get("loader").getAsString()))throw new IllegalArgumentException("Incompatible release");
            String hash=manifest.get("sha256").getAsString();if(!hash.matches("[0-9a-f]{64}"))throw new IllegalArgumentException("Invalid checksum");
            if(mod.getOrigin().getPaths().size()!=1)throw new IllegalStateException("Unsupported installation");
            Path target=mod.getOrigin().getPaths().getFirst().toAbsolutePath().normalize();
            if(!Files.isRegularFile(target)||!target.getFileName().toString().endsWith(".jar")||Files.isSymbolicLink(target))throw new IllegalStateException("Install the release jar first");
            Path stage=FabricLoader.getInstance().getGameDir().resolve(".pulsevisual-updates").toAbsolutePath();Files.createDirectories(stage);
            Path jar=stage.resolve("pulsevisual-"+version+".jar");
            Files.write(jar,download(http,BASE+"download/v"+version+"/pulsevisual-"+version+".jar",16*1024*1024));
            if(!UpdateInstaller.hash(jar).equals(hash)){Files.deleteIfExists(jar);throw new IllegalStateException("Checksum mismatch");}
            validateJar(jar,version);
            Path helper=stage.resolve("helper"),clazz=helper.resolve("dev/pulsevisual/update/UpdateInstaller.class");Files.createDirectories(clazz.getParent());
            try(var in=UpdateInstaller.class.getResourceAsStream("UpdateInstaller.class")){if(in==null)throw new IllegalStateException("Missing installer");Files.copy(in,clazz,StandardCopyOption.REPLACE_EXISTING);}
            pending=new Pending(target,jar,helper,UpdateInstaller.hash(target),hash);
            status="Downloaded "+version+"; restart Minecraft";
            Minecraft.getInstance().execute(()->{var player=Minecraft.getInstance().player;if(player!=null)player.displayClientMessage(Component.literal("PulseVisual: "+status),false);});
        }catch(Exception e){status="Update unavailable: "+e.getMessage();System.err.println("[PulseVisual] "+status);}
    }
    public static void onStopping(){
        Pending p=pending;if(p==null||!ConfigManager.get().automaticUpdates)return;
        try{
            String executable=System.getProperty("os.name").startsWith("Windows")?"javaw.exe":"java";
            Path java=Path.of(System.getProperty("java.home"),"bin",executable);
            new ProcessBuilder(java.toString(),"-cp",p.helper.toString(),UpdateInstaller.class.getName(),Long.toString(ProcessHandle.current().pid()),p.target.toString(),p.jar.toString(),p.oldHash,p.hash)
                    .redirectErrorStream(true).redirectOutput(p.helper.getParent().resolve("install.log").toFile()).start();
        }catch(Exception e){System.err.println("[PulseVisual] Could not apply update: "+e.getMessage());}
    }
}
