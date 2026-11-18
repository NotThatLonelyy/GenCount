package com.notthatlonely.gencount.client.platform;

public final class Platform {

    // PlatformHooksImpl lives in versions/<mc>/src, one copy per Minecraft version.
    private static final PlatformHooksImpl HOOKS = new PlatformHooksImpl();

    private Platform() {}

    public static PlatformHooks hooks() {
        return HOOKS;
    }
}
