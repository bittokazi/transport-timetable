// main.js
import {
  createWidget,
  widget,
  align,
  text_style,
  event,
  prop,
  setStatusBarVisible,
} from "@zos/ui";
import { getDeviceInfo } from "@zos/device";
import { getPackageInfo } from "@zos/app";
import { push } from "@zos/router";
import { BasePage } from "@zeppos/zml/base-page";
import { setWakeUpRelaunch } from "@zos/display";
import { Geolocation } from "@zos/sensor";
import { showToast } from "@zos/interaction";
import { APP_VERSION } from "./../common/version";

Page(
    BasePage({
      keepAlive: true,
      state: {
        selectedDate: "",
        selectedTime: "",
        fromTextWidget: null,
        toTextWidget: null,
        dateTextWidget: null,
        timeTextWidget: null,
        geolocation: null,
        geoTimeout: null,
        isLocating: false,
        activeTarget: null,
      },

      onInit() {
        setWakeUpRelaunch({
          relaunch: true,
        });
        const app = getApp();
        const globalData = app.globalData || {};

        const now = new Date();
        const yyyy = now.getFullYear();
        const mm = String(now.getMonth() + 1).padStart(2, "0");
        const dd = String(now.getDate()).padStart(2, "0");
        const hh = String(now.getHours()).padStart(2, "0");
        const min = String(now.getMinutes()).padStart(2, "0");

        this.state.selectedDate =
            globalData["routeDate"] || `${yyyy}-${mm}-${dd}`;
        this.state.selectedTime = globalData["routeTime"] || `${hh}:${min}`;
      },

      onResume() {
        this.syncSelectionUI();

        const app = getApp();
        const globalData = app.globalData || {};

        if (
            globalData["routeDate"] &&
            globalData["routeDate"] !== this.state.selectedDate
        ) {
          this.state.selectedDate = globalData["routeDate"];
          if (this.state.dateTextWidget) {
            this.state.dateTextWidget.setProperty(
                prop.TEXT,
                this.state.selectedDate
            );
          }
        }

        if (
            globalData["routeTime"] &&
            globalData["routeTime"] !== this.state.selectedTime
        ) {
          this.state.selectedTime = globalData["routeTime"];
          if (this.state.timeTextWidget) {
            this.state.timeTextWidget.setProperty(
                prop.TEXT,
                this.state.selectedTime
            );
          }
        }
      },

      onDestroy() {
        this.cleanupGeolocation(true);
      },

      syncSelectionUI() {
        const app = getApp();
        const globalData = app.globalData || {};

        if (this.state.fromTextWidget) {
          const fromSelection = globalData["fromSelection"];
          const fromName =
              fromSelection && fromSelection.name
                  ? fromSelection.name
                  : "Select From";
          this.state.fromTextWidget.setProperty(prop.MORE, {
            text: fromName,
            color: fromSelection ? 0xffffff : 0x999999,
          });
        }

        if (this.state.toTextWidget) {
          const toSelection = globalData["toSelection"];
          const toName =
              toSelection && toSelection.name ? toSelection.name : "Select To";
          this.state.toTextWidget.setProperty(prop.MORE, {
            text: toName,
            color: toSelection ? 0xffffff : 0x999999,
          });
        }
      },

      swapLocations() {
        const app = getApp();
        if (!app.globalData) app.globalData = {};

        const temp = app.globalData["fromSelection"];
        app.globalData["fromSelection"] = app.globalData["toSelection"];
        app.globalData["toSelection"] = temp;

        this.syncSelectionUI();
      },

      cleanupGeolocation(immediate = false) {
        this.state.isLocating = false;
        this.state.activeTarget = null;

        if (this.state.geoTimeout) {
          clearTimeout(this.state.geoTimeout);
          this.state.geoTimeout = null;
        }

        const stopSensor = () => {
          if (this.state.geolocation) {
            try {
              this.state.geolocation.stop();
            } catch (e) {}
          }
        };

        if (immediate) {
          stopSensor();
        } else {
          setTimeout(stopSensor, 500);
        }
      },

      fetchCurrentLocation(targetType) {
        if (this.state.isLocating) {
          showToast({ content: "Already getting location..." });
          return;
        }

        this.state.isLocating = true;
        this.state.activeTarget = targetType;

        const targetWidget =
            targetType === "FROM"
                ? this.state.fromTextWidget
                : this.state.toTextWidget;

        if (targetWidget) {
          targetWidget.setProperty(prop.MORE, {
            text: "Locating...",
            color: 0x29b6f6,
          });
        }

        try {
          if (!this.state.geolocation) {
            this.state.geolocation = new Geolocation();
            this.state.geolocation.onChange(() => {
              if (!this.state.isLocating || !this.state.geolocation) return;

              if (this.state.geolocation.getStatus() === "A") {
                const lat = this.state.geolocation.getLatitude();
                const lon = this.state.geolocation.getLongitude();

                if (
                    typeof lat === "number" &&
                    typeof lon === "number" &&
                    !isNaN(lat) &&
                    !isNaN(lon) &&
                    !(lat === 0 && lon === 0)
                ) {
                  const target = this.state.activeTarget;
                  this.cleanupGeolocation(false);

                  const app = getApp();
                  if (!app.globalData) app.globalData = {};

                  const locationObj = {
                    name: "Current Location",
                    lat: lat,
                    lon: lon,
                    latitude: lat,
                    longitude: lon,
                  };

                  if (target === "FROM") {
                    app.globalData["fromSelection"] = locationObj;
                  } else if (target === "TO") {
                    app.globalData["toSelection"] = locationObj;
                  }

                  this.syncSelectionUI();
                  showToast({ content: "Location updated" });
                }
              }
            });
          }

          this.state.geolocation.start();

          this.state.geoTimeout = setTimeout(() => {
            if (this.state.isLocating) {
              this.cleanupGeolocation(false);
              this.syncSelectionUI();
              showToast({ content: "GPS timeout - try outdoors" });
            }
          }, 20000);
        } catch (e) {
          this.cleanupGeolocation(false);
          this.syncSelectionUI();
          showToast({ content: "GPS error" });
        }
      },

      build() {
        setStatusBarVisible(false);
        const app = getApp();
        const globalData = app.globalData || {};

        const { width: deviceWidth } = getDeviceInfo();
        const containerWidth = Math.min(380, Math.round(deviceWidth * 0.82));
        const containerX = Math.round((deviceWidth - containerWidth) / 2);

        const locBtnWidth = 64;
        const locBtnGap = 8;
        const inputBoxWidth = containerWidth - locBtnWidth - locBtnGap;
        const locBtnX = containerX + inputBoxWidth + locBtnGap;

        let currentY = Math.round(deviceWidth * 0.09);
        const fromRowY = currentY;

        // 1. "Select From" Element + Location Button
        const fromText = globalData["fromSelection"]?.name || "Select From";

        createWidget(widget.BUTTON, {
          x: containerX,
          y: currentY,
          w: inputBoxWidth,
          h: 68,
          radius: 18,
          normal_color: 0x1a1a1a,
          press_color: 0x2d2d2d,
        });

        this.state.fromTextWidget = createWidget(widget.TEXT, {
          x: containerX + 14,
          y: currentY,
          w: inputBoxWidth - 28,
          h: 68,
          color: globalData["fromSelection"] ? 0xffffff : 0x999999,
          text_size: 24,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text_style: text_style.ELLIPSIS,
          text: fromText,
        });

        const fromClickTarget = createWidget(widget.TEXT, {
          x: containerX,
          y: currentY,
          w: inputBoxWidth,
          h: 68,
          text: "",
        });

        fromClickTarget.addEventListener(event.CLICK_DOWN, () => {
          push({ url: "page/input", params: { type: "FROM" } });
        });

        const fromLocBtn = createWidget(widget.BUTTON, {
          x: locBtnX,
          y: currentY,
          w: locBtnWidth,
          h: 68,
          radius: 18,
          normal_color: 0x132638,
          press_color: 0x1f3f5c,
          text: "⊙",
          text_size: 28,
          color: 0x29b6f6,
        });

        fromLocBtn.addEventListener(event.CLICK_DOWN, () => {
          this.fetchCurrentLocation("FROM");
        });

        currentY += 80;
        const toRowY = currentY;

        // 2. "Select To" Element + Location Button
        const toText = globalData["toSelection"]?.name || "Select To";

        createWidget(widget.BUTTON, {
          x: containerX,
          y: currentY,
          w: inputBoxWidth,
          h: 68,
          radius: 18,
          normal_color: 0x1a1a1a,
          press_color: 0x2d2d2d,
        });

        this.state.toTextWidget = createWidget(widget.TEXT, {
          x: containerX + 14,
          y: currentY,
          w: inputBoxWidth - 28,
          h: 68,
          color: globalData["toSelection"] ? 0xffffff : 0x999999,
          text_size: 24,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text_style: text_style.ELLIPSIS,
          text: toText,
        });

        const toClickTarget = createWidget(widget.TEXT, {
          x: containerX,
          y: currentY,
          w: inputBoxWidth,
          h: 68,
          text: "",
        });

        toClickTarget.addEventListener(event.CLICK_DOWN, () => {
          push({ url: "page/input", params: { type: "TO" } });
        });

        const toLocBtn = createWidget(widget.BUTTON, {
          x: locBtnX,
          y: currentY,
          w: locBtnWidth,
          h: 68,
          radius: 18,
          normal_color: 0x132638,
          press_color: 0x1f3f5c,
          text: "⊙",
          text_size: 28,
          color: 0x29b6f6,
        });

        toLocBtn.addEventListener(event.CLICK_DOWN, () => {
          this.fetchCurrentLocation("TO");
        });

        // 2.5 Floating Swap Button centered between From & To input boxes
        const swapSize = 44;
        const swapX = containerX + Math.round((inputBoxWidth - swapSize) / 2);
        const swapY = Math.round((fromRowY + 68 + toRowY - swapSize) / 2);

        const swapBtn = createWidget(widget.BUTTON, {
          x: swapX,
          y: swapY,
          w: swapSize,
          h: swapSize,
          radius: Math.round(swapSize / 2),
          normal_color: 0x2c2c2c,
          press_color: 0x454545,
          text: "⇅",
          text_size: 22,
          color: 0x00e676,
        });

        swapBtn.addEventListener(event.CLICK_DOWN, () => {
          this.swapLocations();
        });

        currentY += 80;

        // 3. Date & Time Action Selectors
        const splitWidth = Math.round(containerWidth / 2) - 8;

        createWidget(widget.BUTTON, {
          x: containerX,
          y: currentY,
          w: splitWidth,
          h: 60,
          radius: 16,
          normal_color: 0x24190b,
          press_color: 0x3d2b14,
        });

        this.state.dateTextWidget = createWidget(widget.TEXT, {
          x: containerX,
          y: currentY,
          w: splitWidth,
          h: 60,
          color: 0xffa726,
          text_size: 22,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text_style: text_style.NONE,
          text: this.state.selectedDate,
        });

        const dateWidget = createWidget(widget.TEXT, {
          x: containerX,
          y: currentY,
          w: splitWidth,
          h: 60,
          text: "",
        });

        dateWidget.addEventListener(event.CLICK_DOWN, () => {
          push({ url: "page/date_pick" });
        });

        createWidget(widget.BUTTON, {
          x: containerX + splitWidth + 16,
          y: currentY,
          w: splitWidth,
          h: 60,
          radius: 16,
          normal_color: 0x0a1b24,
          press_color: 0x122e3d,
        });

        this.state.timeTextWidget = createWidget(widget.TEXT, {
          x: containerX + splitWidth + 16,
          y: currentY,
          w: splitWidth,
          h: 60,
          color: 0x29b6f6,
          text_size: 22,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text_style: text_style.NONE,
          text: this.state.selectedTime,
        });

        const timeWidget = createWidget(widget.TEXT, {
          x: containerX + splitWidth + 16,
          y: currentY,
          w: splitWidth,
          h: 60,
          text: "",
        });

        timeWidget.addEventListener(event.CLICK_DOWN, () => {
          push({ url: "page/time_pick" });
        });

        currentY += 76;

        // 4. "Search" Action Button
        const actionButtonWidth = containerWidth;
        const actionButtonX = containerX;

        const showRouteWidget = createWidget(widget.BUTTON, {
          x: actionButtonX,
          y: currentY,
          w: actionButtonWidth,
          h: 64,
          radius: 32,
          normal_color: 0x00e676,
          press_color: 0x00c853,
          text: "Search",
          text_size: 24,
          color: 0x000000,
        });

        showRouteWidget.addEventListener(event.CLICK_DOWN, () => {
          const freshGlobalData = getApp().globalData || {};
          const verifiedFrom = freshGlobalData["fromSelection"];
          const verifiedTo = freshGlobalData["toSelection"];

          if (verifiedFrom && verifiedTo) {
            push({
              url: "page/routes",
              params: {
                from: verifiedFrom,
                to: verifiedTo,
                date: this.state.selectedDate,
                time: this.state.selectedTime,
                token: freshGlobalData["accessToken"],
              },
            });
          }
        });

        const favoritesButton = createWidget(widget.BUTTON, {
          x: actionButtonX,
          y: currentY + 74,
          w: actionButtonWidth,
          h: 50,
          radius: 20,
          normal_color: 0x1f3c5c,
          press_color: 0x2f557f,
          text: "Manage Favorites",
          text_size: 20,
          color: 0xffffff,
        });

        favoritesButton.addEventListener(event.CLICK_DOWN, () => {
          push({ url: "page/favorites" });
        });

        // 5. App Version Number at the Bottom
        const versionStr = `v${APP_VERSION}`;

        createWidget(widget.TEXT, {
          x: containerX,
          y: currentY + 134,
          w: containerWidth,
          h: 32,
          color: 0xffffff,
          text_size: 16,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text_style: text_style.NONE,
          text: versionStr,
        });
      },
    })
);
