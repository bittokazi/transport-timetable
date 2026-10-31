// routes.js
import hmUI, {setStatusBarVisible} from "@zos/ui";
import { widget, align, event } from "@zos/ui";
import { getDeviceInfo } from "@zos/device";
import { back, push } from "@zos/router";
import { BasePage } from "@zeppos/zml/base-page";
import { log } from "@zos/utils";
import { setWakeUpRelaunch } from "@zos/display";
import { iconMap } from "./lib/image_map";
import appConfig from "./../common/config";

// Pure text representations for testing fallback
const textIconMap = {
  walk: "WK",
  bus: "BUS",
  rail: "S",
  subway: "U",
  arrow: ">",
  default: "?",
};

const assetMap = iconMap;

Page(
    BasePage({
      keepAlive: true,
      state: {},
      onInit(param) {
        setWakeUpRelaunch({
          relaunch: true,
        });
        if (param) {
          try {
            const parsed = typeof param === "string" ? JSON.parse(param) : param;
            if (parsed) {
              this.state.from = parsed.from;
              this.state.to = parsed.to;
              // ----------------------------------------------------
              // Read incoming date and time selection params
              // ----------------------------------------------------
              this.state.date = parsed.date || "";
              this.state.time = parsed.time || "";
            }
          } catch (e) {
            log.error("Failed to parse navigation parameters", e);
          }
        }
      },
      build() {
        setStatusBarVisible(false);
        const { width: SCREEN_W, height: SCREEN_H } = getDeviceInfo();
        let currentY = Math.round(SCREEN_H * 0.1);

        const margin = Math.round(SCREEN_W * 0.06);
        const usableW = SCREEN_W - (margin * 2);

        // Current Time Display
        const timeWidget = hmUI.createWidget(widget.TEXT, {
          x: margin,
          y: Math.round(SCREEN_H * 0.02),
          w: usableW,
          h: 30,
          text: "--:--",
          text_size: 26,
          color: 0xffffff,
          align_h: align.CENTER_H,
        });

        // Helper to update current time
        const updateCurrentTime = () => {
          const now = new Date();
          const hh = String(now.getHours()).padStart(2, "0");
          const mm = String(now.getMinutes()).padStart(2, "0");

          timeWidget.setProperty(hmUI.prop.MORE, {
            text: `${hh}:${mm}`,
          });
        };

        // Initial render
        updateCurrentTime();

        // Refresh every minute
        setInterval(updateCurrentTime, 60000);

        // Ensure globalData is fully initialized before checking sub-properties
        const app = getApp();
        if (!app.globalData) app.globalData = {};

        // Helper to calculate total duration in minutes
        function calculateDuration(start, end) {
          const diff = new Date(end) - new Date(start);
          return Math.round(diff / 60000);
        }

        // Helper to extract HH:MM safely from ISO string
        function formatTime(isoString) {
          if (!isoString || isoString.length < 16) return "--:--";
          return isoString.substring(11, 16);
        }

        // Helper to safely format output string based on relative minutes
        function formatRelativeOutput(diffMins) {
          if (diffMins > 0) {
            if (diffMins < 60) {
              return `In ${diffMins} min`;
            } else {
              const hours = Math.floor(diffMins / 60);
              const mins = diffMins % 60;
              return mins > 0 ? `In ${hours}h ${mins}m` : `In ${hours}h`;
            }
          } else if (diffMins === 0) {
            return "Now";
          } else {
            return "Passed";
          }
        }

        // Helper to calculate "In X min" from now until trip start safely across timezones
        function getRelativeStartTime(startIsoString) {
          if (!startIsoString) return "";

          const now = new Date();
          const start = new Date(startIsoString);

          let diffMs = start.getTime() - now.getTime();

          if (Math.abs(diffMs) > 2000000000) {
            const nowMins = now.getHours() * 60 + now.getMinutes();

            const startParts = startIsoString.split("T")[1] || "";
            const startH = parseInt(startParts.substring(0, 2), 10);
            const startM = parseInt(startParts.substring(3, 5), 10);
            const startMins = startH * 60 + startM;

            let diffMins = startMins - nowMins;

            if (diffMins < -600) diffMins += 1440;

            return formatRelativeOutput(diffMins);
          }

          const diffMins = Math.round(diffMs / 60000);
          return formatRelativeOutput(diffMins);
        }

        // Main Header Title
        hmUI.createWidget(widget.TEXT, {
          x: margin,
          y: currentY,
          w: usableW,
          h: 40,
          text: "Available Routes",
          text_size: 26,
          color: 0xffffff,
          align_h: align.CENTER_H,
        });

        currentY += 55;

        // ----------------------------------------------------
        // Format Date & Time into MOTIS ISO 8601 format (with URL Encoding)
        // ----------------------------------------------------
        let formattedMotisTime = "";
        if (this.state.date && this.state.time) {
          const rawIso = `${this.state.date}T${this.state.time}:00Z`;
          formattedMotisTime = encodeURIComponent(rawIso);
        }

        // ----------------------------------------------------
        // Format Location Payloads for Backend Parsing
        // ----------------------------------------------------
        const getId = (id) => {
          if (id.includes("way") || id.includes("node")) {
            return "";
          } else return id;
        };

        const fromPayload = JSON.stringify({
          id: getId(this.state.from.id || ""),
          lat: this.state.from.lat,
          lon: this.state.from.lon,
        });

        const toPayload = JSON.stringify({
          id: getId(this.state.to.id || ""),
          lat: this.state.to.lat,
          lon: this.state.to.lon,
        });

        const queryStr = `from=${encodeURIComponent(fromPayload)}&to=${encodeURIComponent(toPayload)}&time=${formattedMotisTime}&arriveBy=false`;

        // Isolated UI rendering engine
        const renderUI = (data) => {
          if (
              !data?.data?.plan?.itineraries ||
              data.data.plan.itineraries.length === 0
          ) {
            hmUI.createWidget(widget.TEXT, {
              x: margin,
              y: currentY + 40,
              w: usableW,
              h: 100,
              text: data?.message ? data.message : "No routes found",
              text_size: 24,
              color: 0xaaaaaa,
              align_h: align.CENTER_H,
            });
            currentY += 100;
          } else {
            data.data.plan.itineraries.forEach((itinerary, index) => {
              const CARD_W = usableW;
              const CARD_H = 145;
              const CARD_X = margin;

              // 1. Base Card Layout Background Shape
              hmUI.createWidget(widget.BUTTON, {
                x: CARD_X,
                y: currentY,
                w: CARD_W,
                h: CARD_H,
                radius: 20,
                normal_color: 0x262626,
                press_color: 0x333333,
              });

              // 2. Render Departure & Arrival Times
              const timeString = `${formatTime(itinerary.start)} - ${formatTime(itinerary.end)}`;
              hmUI.createWidget(widget.TEXT, {
                x: CARD_X + 20,
                y: currentY + 15,
                w: Math.round(CARD_W * 0.55),
                h: 30,
                text: timeString,
                text_size: 20,
                color: 0xffffff,
                align_v: align.CENTER_V,
              });

              // 3. Render Calculated Total Duration
              const durationMinutes = calculateDuration(
                  itinerary.start,
                  itinerary.end,
              );
              hmUI.createWidget(widget.TEXT, {
                x: CARD_X + CARD_W - 130,
                y: currentY + 15,
                w: 110,
                h: 30,
                text: `${durationMinutes} min`,
                text_size: 18,
                color: 0x00ff88,
                align_h: align.RIGHT,
                align_v: align.CENTER_V,
              });

              // 4. Horizontal Leg Timeline (Enlarged Icons & Text)
              let startIconX = CARD_X + 20;
              const MAX_X = CARD_X + CARD_W - 20;

              const ICON_SIZE = 30;
              const TEXT_W = 50;
              const STEP_TOTAL_W = ICON_SIZE + 4 + TEXT_W;
              const ARROW_W = 14;
              const TIMELINE_Y = currentY + 55;

              for (let i = 0; i < itinerary.legs.length; i++) {
                const leg = itinerary.legs[i];
                const modeLower = leg.mode.toLowerCase();

                if (startIconX + STEP_TOTAL_W > MAX_X) {
                  hmUI.createWidget(widget.TEXT, {
                    x: startIconX,
                    y: TIMELINE_Y,
                    w: 30,
                    h: ICON_SIZE,
                    text: "...",
                    text_size: 20,
                    color: 0x888888,
                    align_v: align.CENTER_V,
                  });
                  break;
                }

                const imageFile = assetMap[modeLower] || assetMap.default;
                hmUI.createWidget(widget.IMG, {
                  x: startIconX,
                  y: TIMELINE_Y,
                  w: ICON_SIZE,
                  h: ICON_SIZE,
                  src: imageFile,
                  auto_scale: true,
                });

                const lineIdentifier =
                    leg.route?.shortName ||
                    textIconMap[modeLower] ||
                    textIconMap.default;
                hmUI.createWidget(widget.TEXT, {
                  x: startIconX + ICON_SIZE + 4,
                  y: TIMELINE_Y,
                  w: TEXT_W,
                  h: ICON_SIZE,
                  text: lineIdentifier,
                  text_size: 18,
                  color: modeLower === "walk" ? 0xbbbbbb : 0xffffff,
                  align_h: align.LEFT,
                  align_v: align.CENTER_V,
                });

                startIconX += STEP_TOTAL_W;

                if (i < itinerary.legs.length - 1) {
                  if (startIconX + ARROW_W + 8 > MAX_X) {
                    hmUI.createWidget(widget.TEXT, {
                      x: startIconX,
                      y: TIMELINE_Y,
                      w: 30,
                      h: ICON_SIZE,
                      text: "...",
                      text_size: 20,
                      color: 0x888888,
                      align_v: align.CENTER_V,
                    });
                    break;
                  }

                  hmUI.createWidget(widget.IMG, {
                    x: startIconX,
                    y: TIMELINE_Y + (ICON_SIZE / 2 - 5),
                    w: 10,
                    h: 10,
                    src: assetMap.arrow,
                    auto_scale: true,
                  });

                  startIconX += ARROW_W + 4;
                }
              }

              // 5. Dynamic relative countdown info (Bottom-Left)
              const relativeTimeStr = getRelativeStartTime(itinerary.start);
              hmUI.createWidget(widget.TEXT, {
                x: CARD_X + 20,
                y: currentY + CARD_H - 35,
                w: 200,
                h: 25,
                text: relativeTimeStr,
                text_size: 16,
                color: 0xffaa00,
                align_h: align.LEFT,
                align_v: align.CENTER_V,
              });

              // Transparent full-card touch hitbox over everything
              const cardHitbox = hmUI.createWidget(widget.TEXT, {
                x: CARD_X,
                y: currentY,
                w: CARD_W,
                h: CARD_H,
                text: "",
              });

              cardHitbox.addEventListener(event.CLICK_DOWN, () => {
                push({
                  url: "page/route",
                  params: {
                    trip: itinerary,
                    from: this.state.from,
                    to: this.state.to,
                  },
                });
              });

              currentY += CARD_H + 15;
            });
          }

          currentY += 20;

          // Persistent Global Back Control Button
          const btnW = Math.min(360, usableW);
          const btnX = Math.round((SCREEN_W - btnW) / 2);

          hmUI.createWidget(widget.BUTTON, {
            x: btnX,
            y: currentY,
            w: btnW,
            h: 60,
            radius: 30,
            text: "Back",
            text_size: 22,
            normal_color: 0x222222,
            press_color: 0x333333,
            click_func: () => {
              back();
            },
          });
        };

        // Cache Core Validation Engine
        if (
            app.globalData.routeCacheData &&
            app.globalData.routeCacheQuery === queryStr
        ) {
          log.info("Rendering routes directly from persistent globalApp cache");
          renderUI(app.globalData.routeCacheData);
        } else {
          log.info(
              "Cache miss or fresh query coordinates/time. Fetching from API endpoint...",
          );
          this.fetchData(queryStr, (data) => {
            if (data?.data?.plan?.itineraries) {
              app.globalData.routeCacheData = data;
              app.globalData.routeCacheQuery = queryStr;
            }
            renderUI(data);
          });
        }
      },
      fetchData(query, cb) {
        log.info("Fetching data for trip with parameters:", query);

        const app = getApp();
        const token =
            app.globalData && app.globalData["accessToken"]
                ? app.globalData["accessToken"]
                : "";

        this.httpRequest({
          method: "GET",
          url: `${appConfig.url}/api/v1/transport/plan?${query}`,
          headers: {
            Authorization: `Bearer ${token}`,
            Accept: "application/json",
          },
        })
            .then((result) => {
              log.info("Received result status body data.");
              const responseBody =
                  typeof result.body === "string"
                      ? JSON.parse(result.body)
                      : result.body;
              cb(responseBody);
            })
            .catch((err) => {
              log.error("Error fetching protected data itinerary:", err);
              if (err.data && err.data.error == "limit_reached") {
                cb({
                  error: err.data.message || "Unknown error",
                });
              } else {
                cb(null);
              }
            });
      },
    }),
);
