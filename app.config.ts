import "./scripts/load-env.js";
import type { ExpoConfig } from "expo/config";

const rawBundleId = "space.manus.max.seg.apiahy.t20260910020105";
const bundleId = rawBundleId.replace(/[-_]/g, ".").replace(/[^a-zA-Z0-9.]/g, "").replace(/\.+/g, ".").replace(/^\.+|\.+$/g, "");
const scheme = `manus${bundleId.split(".").pop()?.replace(/^t/, "") ?? ""}`;

const config: ExpoConfig = {
  name: "Max Seg & Max Saúde — Apiahy",
  slug: "max-seg-apiahy",
  version: "1.0.0",
  orientation: "portrait",
  icon: "./assets/images/icon.png",
  scheme,
  userInterfaceStyle: "dark",
  newArchEnabled: true,
  ios: { supportsTablet: true, bundleIdentifier: bundleId, infoPlist: { ITSAppUsesNonExemptEncryption: false } },
  android: {
    adaptiveIcon: {
      backgroundColor: "#080808",
      foregroundImage: "./assets/images/android-icon-foreground.png",
      backgroundImage: "./assets/images/android-icon-background.png",
      monochromeImage: "./assets/images/android-icon-monochrome.png",
    },
    edgeToEdgeEnabled: true,
    predictiveBackGestureEnabled: false,
    package: bundleId,
    permissions: ["POST_NOTIFICATIONS"],
  },
  web: {
    bundler: "metro",
    output: "static",
    favicon: "./assets/images/favicon.png",
    name: "Max Seg & Max Saúde — Apiahy",
    shortName: "Max Seg Apiahy",
    themeColor: "#080808",
    backgroundColor: "#080808",
    lang: "pt-BR",
  },
  plugins: [
    "expo-router",
    ["expo-location", { locationWhenInUsePermission: "Permita que a Max Seg use sua localização para acionar a pronta resposta SOS." }],
    ["expo-splash-screen", { image: "./assets/images/splash-icon.png", imageWidth: 200, resizeMode: "contain", backgroundColor: "#080808" }],
  ],
  experiments: { typedRoutes: true, reactCompiler: true },
};

export default config;
