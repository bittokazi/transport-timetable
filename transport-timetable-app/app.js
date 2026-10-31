import { BaseApp } from "@zeppos/zml/base-app";

App(
  BaseApp({
    globalData: {},
    onCreate() {
      console.log("App created");
    },
    onDestroy() {
      console.log("App destroyed");
    },
  }),
);
