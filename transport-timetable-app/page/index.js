// page/index.js
import {createWidget, widget, align, text_style, event, prop, setStatusBarVisible} from "@zos/ui";
import { getDeviceInfo } from "@zos/device";
import { replace } from "@zos/router";
import { BasePage } from "@zeppos/zml/base-page";
import { log } from "@zos/utils";
import {setWakeUpRelaunch} from "@zos/display";

Page(
    BasePage({
      keepAlive: true,
      state: {
        isChecking: true,
        errorText: "",
        spinnerAnim: null,
        spinnerAngle: 0,
        activeWidgets: [], // Safely hold references
      },

      onInit() {
        setWakeUpRelaunch({
          relaunch: true,
        });
        log.info("Page onInit triggered.");
      },

      // Completely safe layout wiper that forces a screen canvas redraw
      clearUI() {
        // 1. Delete old native widgets from memory
        this.state.activeWidgets.forEach((w) => {
          if (w) {
            try {
              widget.deleteWidget(w);
            } catch (e) {
              log.error("Error destroying widget:", e);
            }
          }
        });
        this.state.activeWidgets = [];

        // 2. FORCE FLASH THE SCREEN BACKGROUND
        // Drawing a black full-screen rectangle instantly clears lingering ghost artifacts
        const { width: deviceWidth, height: deviceHeight } = getDeviceInfo();

        const bgCleaner = createWidget(widget.FILL_RECT, {
          x: 0,
          y: 0,
          w: deviceWidth,
          h: deviceHeight,
          color: 0x000000,
        });

        // Instantly delete it—its draw pass has already overwritten the screen buffer
        try {
          widget.deleteWidget(bgCleaner);
        } catch (e) {}
      },

      verifyAuthentication() {
        log.info("Starting authentication token lookup...");

        this.request({
          method: "GET_TOKEN",
        })
            .then((data) => {
              log.info("Received response from app side:", data);

              if (
                  data &&
                  data.token &&
                  data.expiry &&
                  data.token !== "" &&
                  data.expiry !== ""
              ) {
                const currentTime = Date.now();

                if (currentTime < Number(data.expiry)) {
                  log.info("Token verified successfully. Redirecting...");
                  const app = getApp();
                  app.globalData = app.globalData || {};
                  app.globalData["accessToken"] = data.token;

                  // Tear down completely before executing page route changes
                  this.clearSpinner();
                  this.clearUI();

                  replace({ url: "page/main" });
                  return;
                }
              }

              // Force failure rendering safely
              this.showAuthError(
                  "Session expired.\nPlease open Zepp App\non your phone to login.",
              );
            })
            .catch((err) => {
              console.error("Auth routing failure callback:", err);
              this.showAuthError("Connection error.\nEnsure watch is connected.");
            });
      },

      showAuthError(message) {
        this.state.isChecking = false;
        this.state.errorText = message;
        this.clearSpinner();

        // Clear the loading screen widgets completely and redraw
        this.clearUI();
        this.renderErrorUI();
      },

      clearSpinner() {
        if (this.state.spinnerAnim) {
          clearInterval(this.state.spinnerAnim);
          this.state.spinnerAnim = null;
        }
      },

      build() {
        setStatusBarVisible(false);
        log.info("Build lifecycle method invoked.");

        // Draw the loading state UI instantly
        this.renderLoadingUI();

        // Defer side service query to protect layout phase
        setTimeout(() => {
          this.verifyAuthentication();
        }, 100);
      },

      renderLoadingUI() {
        const { width: deviceWidth, height: deviceHeight } = getDeviceInfo();
        const centerX = Math.round(deviceWidth / 2);
        const centerY = Math.round(deviceHeight / 2);

        // Text Hint
        const loadingText = createWidget(widget.TEXT, {
          x: Math.round(deviceWidth * 0.08),
          y: centerY + Math.round(deviceHeight * 0.1),
          w: deviceWidth - Math.round(deviceWidth * 0.16),
          h: 60,
          color: 0xaaaaaa,
          text_size: 22,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text: "Checking account...",
        });
        this.state.activeWidgets.push(loadingText);

        // Nice Canvas Arc Spinner Widget
        const spinner = createWidget(widget.ARC, {
          x: centerX - 30,
          y: centerY - Math.round(deviceHeight * 0.1),
          w: 60,
          h: 60,
          start_angle: 0,
          end_angle: 90,
          color: 0x00e676,
          line_width: 5,
        });
        this.state.activeWidgets.push(spinner);

        // Rotate the loading element smoothly
        this.clearSpinner();
        this.state.spinnerAnim = setInterval(() => {
          this.state.spinnerAngle = (this.state.spinnerAngle + 15) % 360;

          spinner.setProperty(prop.MORE, {
            start_angle: this.state.spinnerAngle,
            end_angle: (this.state.spinnerAngle + 90) % 360,
          });
        }, 40);
      },

      renderErrorUI() {
        const { width: deviceWidth, height: deviceHeight } = getDeviceInfo();
        const centerX = Math.round(deviceWidth / 2);
        const centerY = Math.round(deviceHeight / 2);

        // Error Message Display
        const errorDisplay = createWidget(widget.TEXT, {
          x: Math.round(deviceWidth * 0.06),
          y: centerY - Math.round(deviceHeight * 0.2),
          w: deviceWidth - Math.round(deviceWidth * 0.12),
          h: Math.round(deviceHeight * 0.25),
          color: 0xff4d4f,
          text_size: 20,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text_style: text_style.WRAP,
          text: this.state.errorText,
        });
        this.state.activeWidgets.push(errorDisplay);

        // Interactive Retry button
        const btnW = Math.min(240, Math.round(deviceWidth * 0.7));
        const retryBtn = createWidget(widget.BUTTON, {
          x: Math.round((deviceWidth - btnW) / 2),
          y: centerY + Math.round(deviceHeight * 0.1),
          w: btnW,
          h: 54,
          radius: 27,
          normal_color: 0x1a1a1a,
          press_color: 0x2d2d2d,
          text: "Retry Link",
          text_size: 20,
          color: 0xffffff,
        });
        this.state.activeWidgets.push(retryBtn);

        retryBtn.addEventListener(event.CLICK_DOWN, () => {
          this.clearSpinner();

          // Clean layout slate and rebuild pristine elements
          this.state.isChecking = true;
          this.clearUI();
          this.renderLoadingUI();

          setTimeout(() => {
            this.verifyAuthentication();
          }, 120);
        });
      },

      onDestroy() {
        this.clearSpinner();
        this.clearUI();
      },
    }),
);
