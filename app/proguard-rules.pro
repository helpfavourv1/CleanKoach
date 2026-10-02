# CleanKoach release shrinker rules.
# Rules are added as features land. Empty by design at scaffold stage.

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
