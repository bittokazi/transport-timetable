// map_view.js - Zepp OS Route Map View Page
import hmUI, {setStatusBarVisible} from "@zos/ui";
import { widget, align, prop } from "@zos/ui";
import { getDeviceInfo } from "@zos/device";
import { back } from "@zos/router";
import { BasePage } from "@zeppos/zml/base-page";
import { log } from "@zos/utils";
import TransferFile from "@zos/ble/TransferFile";
import appConfig from "./../common/config";

const transferFile = new TransferFile();
const inbox = transferFile.getInbox();

Page(
    BasePage({
      state: {
        itinerary: null,
        fromLocation: null,
        toLocation: null,
        headerTextWidget: null,
        mapImageWidget: null,
        loadingTextWidget: null,
        backButtonWidget: null,
        isZoomedIn: false,
      },

      onInit(param) {
        if (param) {
          try {
            const parsed = typeof param === "string" ? JSON.parse(param) : param;
            this.state.itinerary = parsed.trip || null;
            this.state.fromLocation = parsed.from || null;
            this.state.toLocation = parsed.to || null;
          } catch (e) {
            log.error("Failed parsing map routing configuration parameters", e);
          }
        }
        this.getFile();
      },

      build() {
        setStatusBarVisible(false);
        const { width: SCREEN_W, height: SCREEN_H } = getDeviceInfo();

        // Responsive calculations based on device dimensions
        const headerX = Math.round(SCREEN_W * 0.06);
        const headerW = SCREEN_W - (headerX * 2);
        const mapX = Math.round(SCREEN_W * 0.08);
        const mapY = Math.round(SCREEN_H * 0.15);
        const mapW = SCREEN_W - (mapX * 2);
        const mapH = Math.round(SCREEN_H * 0.6);

        // 1. Header Navigation Title
        this.state.headerTextWidget = hmUI.createWidget(widget.TEXT, {
          x: headerX,
          y: Math.round(SCREEN_H * 0.03),
          w: headerW,
          h: 40,
          text: "Route Map",
          text_size: Math.round(SCREEN_W * 0.055),
          color: 0xffffff,
          align_h: align.CENTER_H,
        });

        // 2. Loading State Placeholder Text
        this.state.loadingTextWidget = hmUI.createWidget(widget.TEXT, {
          x: mapX,
          y: mapY + Math.round(mapH * 0.4),
          w: mapW,
          h: 50,
          text: "Loading Map View...",
          text_size: 18,
          color: 0x00ff88,
          align_h: align.CENTER_H,
        });

        // 3. Image Widget - Initially blank waiting for incoming file sync
        this.state.mapImageWidget = hmUI.createWidget(widget.IMG, {
          x: mapX,
          y: mapY,
          w: mapW,
          h: mapH,
          src: "",
          auto_scale: true,
        });

        // Register click event layout toggle handler on the map canvas
        this.state.mapImageWidget.addEventListener(hmUI.event.CLICK_DOWN, () => {
          this.toggleMapZoom();
        });

        // 4. Back Button Controls
        const btnW = Math.round(SCREEN_W * 0.75);
        const btnX = Math.round((SCREEN_W - btnW) / 2);
        const btnY = SCREEN_H - Math.round(SCREEN_H * 0.15);
        const btnH = Math.round(SCREEN_H * 0.11);

        this.state.backButtonWidget = hmUI.createWidget(widget.BUTTON, {
          x: btnX,
          y: btnY,
          w: btnW,
          h: btnH,
          radius: Math.round(btnH / 2),
          text: "Back to Details",
          text_size: 18,
          normal_color: 0x222222,
          press_color: 0x333333,
          click_func: () => {
            back();
          },
        });

        this.fetchAndRegisterMapImage();
      },

      toggleMapZoom() {
        const { width: SCREEN_W, height: SCREEN_H } = getDeviceInfo();
        const mapX = Math.round(SCREEN_W * 0.08);
        const mapY = Math.round(SCREEN_H * 0.15);
        const mapW = SCREEN_W - (mapX * 2);
        const mapH = Math.round(SCREEN_H * 0.6);

        this.state.isZoomedIn = !this.state.isZoomedIn;

        if (this.state.isZoomedIn) {
          // --- GO FULL SCREEN ZOOM ---
          if (this.state.headerTextWidget) {
            this.state.headerTextWidget.setProperty(prop.VISIBLE, false);
          }
          if (this.state.backButtonWidget) {
            this.state.backButtonWidget.setProperty(prop.VISIBLE, false);
          }
          if (this.state.loadingTextWidget) {
            this.state.loadingTextWidget.setProperty(prop.VISIBLE, false);
          }

          // Remap layout coordinate bounds to completely fill the screen display
          this.state.mapImageWidget.setProperty(prop.MORE, {
            x: 0,
            y: 0,
            w: SCREEN_W,
            h: SCREEN_H,
          });
        } else {
          // --- RESTORE STANDARD VIEW ---
          this.state.mapImageWidget.setProperty(prop.MORE, {
            x: mapX,
            y: mapY,
            w: mapW,
            h: mapH,
          });

          // Bring back UI navigation and controls
          if (this.state.headerTextWidget) {
            this.state.headerTextWidget.setProperty(prop.VISIBLE, true);
          }
          if (this.state.backButtonWidget) {
            this.state.backButtonWidget.setProperty(prop.VISIBLE, true);
          }
        }
      },

      fetchAndRegisterMapImage() {
        const mapUrl = this.generateStaticMapUrl();
        if (!mapUrl) {
          this.updateStatusText("Data telemetry missing", 0xaa2222);
          return;
        }

        log.info(
            "Requesting companion file fetch sequence across Bluetooth bridge...",
        );

        const app = getApp();
        const token =
            app.globalData && app.globalData["accessToken"]
                ? app.globalData["accessToken"]
                : "";

        this.request({
          method: "DOWNLOAD_MAP",
          params: { url: mapUrl, token: token },
        })
            .then((response) => {
              if (response && response.status === "SUCCESS") {
                this.updateStatusText("Download request received...", 0x00ff88);
              } else {
                this.updateStatusText("Download error failed", 0xaa2222);
              }
            })
            .catch((err) => {
              log.error("Bridge synchronization connection timed out:", err);
              this.updateStatusText("Connection timed out", 0xaa2222);
            });
      },

      updateStatusText(message, hexColor) {
        if (this.state.loadingTextWidget) {
          this.state.loadingTextWidget.setProperty(prop.MORE, {
            text: message,
            color: hexColor,
          });
        }
      },

      generateStaticMapUrl() {
        const itinerary = this.state.itinerary;
        if (!itinerary || !itinerary.legs || itinerary.legs.length === 0)
          return null;

        const coordinates = [];

        if (this.state.fromLocation?.lat) {
          coordinates.push({
            lat: this.state.fromLocation.lat,
            lon: this.state.fromLocation.lon,
          });
        }

        itinerary.legs.forEach((leg) => {
          if (leg.from) {
            coordinates.push({ lat: leg.from.lat, lon: leg.from.lon });
          }
          if (leg.to) {
            coordinates.push({ lat: leg.to.lat, lon: leg.to.lon });
          }
        });

        if (this.state.toLocation?.lat) {
          coordinates.push({
            lat: this.state.toLocation.lat,
            lon: this.state.toLocation.lon,
          });
        }

        if (coordinates.length === 0) return null;

        let url = `${appConfig.url}/api/v1/transport/map?size=400x300&maptype=roadmap&style=element:geometry%7Ccolor:0x212121&style=element:labels.text.stroke%7Ccolor:0x212121&style=element:labels.text.fill%7Ccolor:0x746855&style=feature:administrative.locality%7Celement:labels.text.fill%7Ccolor:0xd59563&style=feature:road%7Celement:geometry%7Ccolor:0x3c3c3c&style=feature:road%7Celement:geometry.stroke%7Ccolor:0x212121&style=feature:transit%7Celement:geometry%7Ccolor:0x2f3948&style=feature:water%7Celement:geometry%7Ccolor:0x17263c`;

        const startPoint = coordinates[0];
        const endPoint = coordinates[coordinates.length - 1];

        url += `&markers=color:green%7Clabel:S%7C${startPoint.lat},${startPoint.lon}`;
        url += `&markers=color:red%7Clabel:E%7C${endPoint.lat},${endPoint.lon}`;

        let pathString = "&path=color:0x00ff88ff%7Cweight:5";
        coordinates.forEach((coord) => {
          pathString += `%7C${coord.lat},${coord.lon}`;
        });

        url += pathString;

        return url;
      },

      onCall(data) {
        if (data.method === "File") {
          this.updateStatusText(data.message, 0x00ff88);
        }
      },

      getFile() {
        let ref = this;
        inbox.on("newfile", function () {
          const fileObject = inbox.getNextFile();
          fileObject.on("progress", (event) => {});

          fileObject.on("change", (event) => {
            if (event.data.readyState === "transferred") {
              ref.updateStatusText("Successfully downloaded map", 0x00ff88);

              if (ref.state.loadingTextWidget) {
                ref.state.loadingTextWidget.setProperty(prop.VISIBLE, false);
              }

              if (ref.state.mapImageWidget) {
                ref.state.mapImageWidget.setProperty(
                    prop.SRC,
                    fileObject.filePath,
                );
              }
            } else if (event.data.readyState == "error") {
              ref.updateStatusText("Error processing map", 0xaa2222);
            }
          });
        });
      },
    }),
);
