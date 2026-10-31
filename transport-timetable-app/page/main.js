// main.js
import {createWidget, widget, align, text_style, event, prop, setStatusBarVisible} from "@zos/ui";
import { getDeviceInfo } from "@zos/device";
import { push } from "@zos/router";
import { Time } from "@zos/sensor";
import { BasePage } from "@zeppos/zml/base-page";
import {setWakeUpRelaunch} from "@zos/display";

Page(
    BasePage({
      keepAlive: true,
      state: {
        selectedDate: "",
        selectedTime: "",
        // Maintain persistent references to update the text layers later
        fromTextWidget: null,
        toTextWidget: null,
        dateTextWidget: null,
        timeTextWidget: null,
      },

      // 1. Setup initial date/time values on creation
      onInit() {
        setWakeUpRelaunch({
          relaunch: true,
        });
        const app = getApp();
        const globalData = app.globalData || {};
        const time = new Time();

        const yyyy = time.getFullYear();
        const mm = String(time.getMonth() + 1).padStart(2, "0");
        const dd = String(time.getDate()).padStart(2, "0");
        const hh = String(time.getHours()).padStart(2, "0");
        const min = String(time.getMinutes()).padStart(2, "0");

        this.state.selectedDate =
            globalData["routeDate"] || `${yyyy}-${mm}-${dd}`;
        this.state.selectedTime = globalData["routeTime"] || `${hh}:${min}`;
      },

      // 2. Dynamically update text properties when page regains focus
      onResume() {
        const app = getApp();
        const globalData = app.globalData || {};

        // Refresh the "From" display
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

        // Refresh the "To" display
        if (this.state.toTextWidget) {
          const toSelection = globalData["toSelection"];
          const toName =
              toSelection && toSelection.name ? toSelection.name : "Select To";
          this.state.toTextWidget.setProperty(prop.MORE, {
            text: toName,
            color: toSelection ? 0xffffff : 0x999999,
          });
        }

        // Refresh Date if updated in globalData
        if (globalData["routeDate"] && globalData["routeDate"] !== this.state.selectedDate) {
          this.state.selectedDate = globalData["routeDate"];
          if (this.state.dateTextWidget) {
            this.state.dateTextWidget.setProperty(prop.TEXT, this.state.selectedDate);
          }
        }

        // Refresh Time if updated in globalData
        if (globalData["routeTime"] && globalData["routeTime"] !== this.state.selectedTime) {
          this.state.selectedTime = globalData["routeTime"];
          if (this.state.timeTextWidget) {
            this.state.timeTextWidget.setProperty(prop.TEXT, this.state.selectedTime);
          }
        }
      },

      build() {
        setStatusBarVisible(false);
        const app = getApp();
        const globalData = app.globalData || {};

        const { width: deviceWidth } = getDeviceInfo();
        const containerWidth = Math.min(380, Math.round(deviceWidth * 0.82));
        const containerX = Math.round((deviceWidth - containerWidth) / 2);

        let currentY = Math.round(deviceWidth * 0.09);

        // ----------------------------------------------------
        // 1. "Select From" Element
        // ----------------------------------------------------
        const fromText = globalData["fromSelection"]?.name || "Select From";

        createWidget(widget.BUTTON, {
          x: containerX,
          y: currentY,
          w: containerWidth,
          h: 68,
          radius: 18,
          normal_color: 0x1a1a1a,
          press_color: 0x2d2d2d,
        });

        this.state.fromTextWidget = createWidget(widget.TEXT, {
          x: containerX + 20,
          y: currentY,
          w: containerWidth - 40,
          h: 68,
          color: globalData["fromSelection"] ? 0xffffff : 0x999999,
          text_size: 26,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text_style: text_style.NONE,
          text: fromText,
        });

        const fromClickTarget = createWidget(widget.TEXT, {
          x: containerX,
          y: currentY,
          w: containerWidth,
          h: 68,
          text: "",
        });

        fromClickTarget.addEventListener(event.CLICK_DOWN, () => {
          push({ url: "page/input", params: { type: "FROM" } });
        });

        currentY += 80;

        // ----------------------------------------------------
        // 2. "Select To" Element
        // ----------------------------------------------------
        const toText = globalData["toSelection"]?.name || "Select To";

        createWidget(widget.BUTTON, {
          x: containerX,
          y: currentY,
          w: containerWidth,
          h: 68,
          radius: 18,
          normal_color: 0x1a1a1a,
          press_color: 0x2d2d2d,
        });

        this.state.toTextWidget = createWidget(widget.TEXT, {
          x: containerX + 20,
          y: currentY,
          w: containerWidth - 40,
          h: 68,
          color: globalData["toSelection"] ? 0xffffff : 0x999999,
          text_size: 26,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text_style: text_style.NONE,
          text: toText,
        });

        const toClickTarget = createWidget(widget.TEXT, {
          x: containerX,
          y: currentY,
          w: containerWidth,
          h: 68,
          text: "",
        });

        toClickTarget.addEventListener(event.CLICK_DOWN, () => {
          push({ url: "page/input", params: { type: "TO" } });
        });

        currentY += 80;

        // ----------------------------------------------------
        // 3. "Date" Action Selector Button
        // ----------------------------------------------------
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

        // ----------------------------------------------------
        // 3.5. "Time" Action Selector Button
        // ----------------------------------------------------
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

        // ----------------------------------------------------
        // 4. "Show Route" Primary Action Button
        // ----------------------------------------------------
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
      },
    }),
);
