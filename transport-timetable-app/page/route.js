// route.js
import hmUI, {setStatusBarVisible} from "@zos/ui";
import { createWidget, widget, align, prop, event } from "@zos/ui";
import { log, px } from "@zos/utils";
import { getDeviceInfo } from "@zos/device";
import { BasePage } from "@zeppos/zml/base-page";
import { back, push } from "@zos/router";
import { setWakeUpRelaunch } from "@zos/display";
import { display } from "@zos/display";

import { iconMap } from "./lib/image_map";

// --- Helper Functions for Time Parsing ---
function formatTime(isoString) {
  if (!isoString) return "--:--";
  try {
    const d = new Date(isoString);
    const hours = String(d.getHours()).padStart(2, "0");
    const minutes = String(d.getMinutes()).padStart(2, "0");
    return `${hours}:${minutes}`;
  } catch (e) {
    return "--:--";
  }
}

function getDuration(startIso, endIso) {
  if (!startIso || !endIso) return "";
  try {
    const start = new Date(startIso);
    const end = new Date(endIso);
    const diffMs = end - start;
    const diffMins = Math.round(diffMs / 60000);
    return `${diffMins} min`;
  } catch (e) {
    return "";
  }
}

Page(
    BasePage({
      state: {},
      onInit(param) {
        setWakeUpRelaunch({
          relaunch: true,
        });

        if (param) {
          try {
            const parsed = typeof param === "string" ? JSON.parse(param) : param;
            if (parsed) {
              this.state.trip = parsed.trip;
              this.state.from = parsed.from;
              this.state.to = parsed.to;
            }
          } catch (e) {
            log.error("Failed to parse navigation parameters", e);
          }
        }
      },
      build() {
        setStatusBarVisible(false);
        try {
          display.setPageBrightTime({ time: 120000 });

          if (typeof display.setAutoBrightnessLoop === "function") {
            display.setAutoBrightnessLoop({
              enable: true,
            });
          }
        } catch (err) {
          log.error("Failed to acquire display wake lock:", err);
        }

        let refSelf = this;
        const { width: SCREEN_W, height: SCREEN_H } = getDeviceInfo();

        let currentY = Math.round(SCREEN_H * 0.125);
        const margin = Math.round(SCREEN_W * 0.06);
        const usableW = SCREEN_W - (margin * 2);

        // Current Time Display
        const timeWidget = hmUI.createWidget(widget.TEXT, {
          x: margin,
          y: Math.round(SCREEN_H * 0.02),
          w: usableW,
          h: 35,
          text: "--:--",
          text_size: 28,
          color: 0xffffff,
          align_h: align.CENTER_H,
        });

        const updateCurrentTime = () => {
          const now = new Date();
          const hh = String(now.getHours()).padStart(2, "0");
          const mm = String(now.getMinutes()).padStart(2, "0");

          timeWidget.setProperty(hmUI.prop.MORE, {
            text: `${hh}:${mm}`,
          });
        };

        updateCurrentTime();
        setInterval(updateCurrentTime, 60000);

        const fromName = this.state.from?.name || "Origin";
        const toName = this.state.to?.name || "Destination";
        const legs = this.state.trip?.legs || [];

        const totalStart = this.state.trip?.start
            ? formatTime(this.state.trip.start)
            : "";
        const totalEnd = this.state.trip?.end
            ? formatTime(this.state.trip.end)
            : "";
        const totalDuration = getDuration(
            this.state.trip?.start,
            this.state.trip?.end,
        );

        // Header Title (Overall Origin -> Destination)
        hmUI.createWidget(widget.TEXT, {
          x: margin,
          y: currentY,
          w: usableW,
          h: 40,
          text: `${fromName} → ${toName}`,
          text_size: 24,
          color: 0xffffff,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
        });

        currentY += 45;

        // Overall Schedule Summary line
        hmUI.createWidget(widget.TEXT, {
          x: margin,
          y: currentY,
          w: usableW,
          h: 35,
          text: totalDuration
              ? `● ${totalStart} - ${totalEnd} (${totalDuration})`
              : "● Live Navigation",
          text_size: 18,
          color: 0x00ff88,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
        });

        currentY += 65;

        log.info("Data rendering for trip legs count:", legs.length);

        // ==========================================
        // 1. RENDER DETAILED CARDS LOOP
        // ==========================================
        legs.forEach((step, index) => {
          const modeType = step.mode || step.type || "walk";
          const icon = iconMap[modeType.toLowerCase()] || iconMap.default;

          const startTimeStr = formatTime(step.start?.scheduledTime);
          const endTimeStr = formatTime(step.end?.scheduledTime);
          const legDuration = getDuration(
              step.start?.scheduledTime,
              step.end?.scheduledTime,
          );

          const fromStation = step.from?.name || "Station";
          const toStation = step.to?.name || "Station";

          let cardTitle = modeType.toUpperCase();
          if (step.route?.shortName) {
            cardTitle = `${modeType.toUpperCase()} ${step.route.shortName}`;
            if (step.headsign) {
              cardTitle += ` → ${step.headsign}`;
            }
          } else if (step.distance) {
            cardTitle += ` (${Math.round(step.distance)}m)`;
          }

          const cardHeight = 175;
          const timelineX = margin + 23;
          const contentX = margin + 70;
          const contentW = usableW - 70;

          // Vertical timeline connector bar line
          if (index !== legs.length - 1) {
            hmUI.createWidget(widget.FILL_RECT, {
              x: timelineX,
              y: currentY + 54,
              w: 3,
              h: cardHeight - 48,
              color: 0x444444,
            });
          }

          // Card Step Core Icon
          hmUI.createWidget(widget.IMG, {
            x: margin + 5,
            y: currentY + 4,
            w: 42,
            h: 42,
            src: icon,
            auto_scale: true,
          });

          // 1. Line/Route Header Title
          hmUI.createWidget(widget.TEXT, {
            x: contentX,
            y: currentY,
            w: contentW,
            h: 34,
            text: cardTitle,
            text_size: 22,
            color: 0xffffff,
            align_h: align.LEFT,
            align_v: align.CENTER_V,
          });

          // 2. Specific leg schedule time window & sub-duration
          hmUI.createWidget(widget.TEXT, {
            x: contentX,
            y: currentY + 38,
            w: contentW,
            h: 30,
            text: `${startTimeStr} - ${endTimeStr} [${legDuration}]`,
            text_size: 18,
            color: 0x00ff88,
            align_h: align.LEFT,
            align_v: align.CENTER_V,
          });

          // 3. Station Origin Information
          hmUI.createWidget(widget.TEXT, {
            x: contentX,
            y: currentY + 74,
            w: contentW,
            h: 28,
            text: `From: ${fromStation}`,
            text_size: 16,
            color: 0xaaaaaa,
            align_h: align.LEFT,
            align_v: align.CENTER_V,
            text_style: hmUI.text_style.NONE,
          });

          // 4. Station Destination Information
          hmUI.createWidget(widget.TEXT, {
            x: contentX,
            y: currentY + 106,
            w: contentW,
            h: 28,
            text: `To: ${toStation}`,
            text_size: 16,
            color: 0xaaaaaa,
            align_h: align.LEFT,
            align_v: align.CENTER_V,
            text_style: hmUI.text_style.NONE,
          });

          currentY += cardHeight;
        });

        currentY += 20;

        // ==========================================
        // 2. ACTION BUTTONS LAYOUT
        // ==========================================
        const btnW = Math.min(360, usableW);
        const btnX = Math.round((SCREEN_W - btnW) / 2);

        // VIEW MAP Action Button
        hmUI.createWidget(widget.BUTTON, {
          x: btnX,
          y: currentY,
          w: btnW,
          h: 62,
          radius: 31,
          text: "View Map",
          text_size: 22,
          normal_color: 0x00e676,
          press_color: 0x00c853,
          color: 0x000000,
          click_func: () => {
            log.info("Navigating to static route map viewport page view.");
            push({
              url: "page/map_view",
              params: {
                trip: refSelf.state.trip,
                from: refSelf.state.from,
                to: refSelf.state.to,
              },
            });
          },
        });

        currentY += 75;

        // GO BACK Button
        hmUI.createWidget(widget.BUTTON, {
          x: btnX,
          y: currentY,
          w: btnW,
          h: 62,
          radius: 31,
          text: "Go Back",
          text_size: 22,
          normal_color: 0x333333,
          press_color: 0x222222,
          click_func: () => {
            console.log("Go back button pressed");
            back();
          },
        });

        // Bottom blank padding spacer for smooth circular scroll clipping
        hmUI.createWidget(widget.TEXT, {
          x: 0,
          y: currentY + 80,
          w: SCREEN_W,
          h: 100,
          text: "",
        });

        console.log("Trip page initialized correctly.");
      },
    }),
);
