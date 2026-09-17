package dev.pulsevisual.config;

public final class VisualConfig {
    public int animationRevision;
    public boolean automaticUpdates = true;
    public boolean normalHitParticles = true;
    public boolean criticalHitParticles = true;
    public boolean hitFlash = true;
    public boolean crosshair = true;
    public boolean customizeCrosshair;
    public boolean crosshairAnimation = true;
    public boolean weaponEffects = true;
    public boolean hudAnimation = true;
    public int particleCount = 18;
    public float intensity = 0.75f;
    public float crosshairSize = 1.0f;
    public float crosshairOpacity = 1.0f;
    public float crosshairAnimationSpeed = 1.0f;
    public ViewModel viewModel = new ViewModel();
    public Swing swing = new Swing();
    public Hat hat = new Hat();
    public Trail trail = new Trail();
    public Theme theme = new Theme();
    public Fire fire = new Fire();
    public Pearl pearl = new Pearl();
    public Hit hit = new Hit();

    public void sanitize() {
        particleCount = Math.max(0, Math.min(64, particleCount));
        intensity = clamp(intensity, 0.1f, 2.0f);
        crosshairSize = clamp(crosshairSize, 0.5f, 2.5f);
        crosshairOpacity = clamp(crosshairOpacity, 0.15f, 1.0f);
        crosshairAnimationSpeed = clamp(crosshairAnimationSpeed, 0.1f, 3.0f);
        if (viewModel == null) viewModel = new ViewModel();
        if (viewModel.right == null) viewModel.right = new Hand();
        if (viewModel.left == null) viewModel.left = new Hand();
        viewModel.right.sanitize(); viewModel.left.sanitize();
        if (swing == null) swing = new Swing(); swing.sanitize();
        if(animationRevision<2){swing.preset="Vanilla";animationRevision=2;}
        if(hit==null)hit=new Hit();hit.sanitize();
        if (hat == null) hat = new Hat(); hat.sanitize();
        if (trail == null) trail = new Trail(); trail.sanitize();
        if (theme == null) theme = new Theme(); theme.sanitize();
        if (fire == null) fire = new Fire(); fire.sanitize();
        if (pearl == null) pearl = new Pearl(); pearl.sanitize();
    }

    private static float clamp(float value, float min, float max) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
    }

    public static final class Hand {
        public float x, y, z, rotX, rotY, rotZ, scale = 1;
        public void sanitize() {
            x = clamp(x, -.4f, .4f); y = clamp(y, -.35f, .35f); z = clamp(z, -.4f, .25f);
            rotX = clamp(rotX, -45, 45); rotY = clamp(rotY, -45, 45); rotZ = clamp(rotZ, -45, 45);
            scale = clamp(scale, .65f, 1.3f);
        }
    }
    public static final class ViewModel {
        public boolean enabled = true;
        public String preset="Custom", target="Both Hands";
        public Hand right = new Hand();
        public Hand left = new Hand();
    }
    public static final class Swing {
        public boolean enabled = true;
        public String preset = "Vanilla";
        public float speed = 1, duration = 0.42f, strength = 1, rotation = 30;
        public float offsetX, offsetY, offsetZ;
        public String easing = "Smoothstep";
        public float easeIn = 0.35f, easeOut = 0.35f;
        public float smoothness = 1;
        public float rotationX=-7.5f, rotationY=7.5f, peak=.36f;
        public void sanitize() {
            speed = clamp(speed, 0.25f, 3); duration = clamp(duration, 0.15f, 1.2f);
            strength = clamp(strength, 0, 2); rotation = clamp(rotation, -120, 120);
            offsetX = clamp(offsetX, -1, 1); offsetY = clamp(offsetY, -1, 1); offsetZ = clamp(offsetZ, -1, 1);
            easeIn = clamp(easeIn, 0, 1); easeOut = clamp(easeOut, 0, 1);
            smoothness = clamp(smoothness, 0, 1);
            rotationX=clamp(rotationX,-90,90);rotationY=clamp(rotationY,-90,90);peak=clamp(peak,.15f,.8f);
        }
    }
    public static final class Hit {
        public String particle="Default", shape="Burst";
        public float spread=1,speed=1,height=.55f,flashDuration=.26f;
        public boolean extraSparks=true;
        public void sanitize(){spread=clamp(spread,.1f,2);speed=clamp(speed,0,3);height=clamp(height,.1f,1);flashDuration=clamp(flashDuration,.05f,1);}
    }
    public static final class Fire {
        public String preset="Vanilla";
        public float size=100;
        public void sanitize(){size=clamp(size,0,100);}
    }
    public static final class Pearl {
        public boolean enabled, themeColor=true, glow=true, rainbow, gradient=true, pulse, clearOnTeleport=true;
        public String mode="Smooth Line", scope="Own Pearls";
        public int color=0xFF85EAF3, secondaryColor=0xFFB285F3;
        public float alpha=.85f,width=.035f,length=3,fadeSpeed=1;
        public void sanitize(){
            alpha=clamp(alpha,0,1);width=clamp(width,.01f,.15f);
            length=clamp(length,.25f,8);fadeSpeed=clamp(fadeSpeed,.25f,3);
        }
    }
    public static final class Hat {
        public boolean enabled;
        public int color = 0xFF85EAF3;
        public float alpha = 0.72f, size = 0.7f, height = 0.12f, rotationSpeed = 0.45f;
        public String style = "Filled + Outline";
        public boolean rainbow, gradient = true;
        public float rainbowSpeed = 1;
        public void sanitize() {
            alpha = clamp(alpha, 0.05f, 1); size = clamp(size, 0.3f, 1.5f);
            height = clamp(height, -0.3f, 0.8f); rotationSpeed = clamp(rotationSpeed, 0, 3);
            rainbowSpeed = clamp(rainbowSpeed, 0.1f, 4);
        }
    }
    public static final class Trail {
        public boolean enabled;
        public String mode = "Ribbon";
        public int color = 0xFF85EAF3, secondaryColor = 0xFFB285F3;
        public float width = 0.38f, length = 2.7f, fadeSpeed = 1, alpha = 0.8f;
        public float glowStrength = 0.55f, smoothness = 0.65f, rainbowSpeed = 1;
        public boolean gradient = true;
        public int maxPoints = 56;
        public float height=.13f,minDistance=.05f,taper=1,pulseSpeed=1,glowWidth=2;
        public boolean sprintOnly,groundOnly;
        public void sanitize() {
            width = clamp(width, 0.04f, 1.4f); length = clamp(length, 0.4f, 8);
            fadeSpeed = clamp(fadeSpeed, 0.25f, 3); alpha = clamp(alpha, 0.05f, 1);
            glowStrength = clamp(glowStrength, 0, 2); smoothness = clamp(smoothness, 0, 1);
            rainbowSpeed = clamp(rainbowSpeed, 0.1f, 4); maxPoints = Math.max(8, Math.min(160, maxPoints));
            height=clamp(height,.03f,1.5f);minDistance=clamp(minDistance,.02f,.3f);taper=clamp(taper,0,1);pulseSpeed=clamp(pulseSpeed,.1f,4);glowWidth=clamp(glowWidth,1,4);
        }
    }
    public static final class Theme {
        public float guiScale=1;
        public String style="Glass", font="Default";
        public boolean animations=true, textShadow=false;
        public String preset = "Dark";
        public int primary = 0xFF172332, accent = 0xFF85EAF3, background = 0xFF080D16;
        public int text = 0xFFE5EDF4, enabled = 0xFF246E78, control = 0xFF85EAF3;
        public float opacity = 0.92f, blur = 0.55f, rounding = 8;
        public void sanitize() {
            guiScale=clamp(guiScale,.65f,1.5f);
            opacity = clamp(opacity, 0.45f, 1); blur = clamp(blur, 0, 1);
            rounding = clamp(rounding, 0, 16);
        }
    }
}
