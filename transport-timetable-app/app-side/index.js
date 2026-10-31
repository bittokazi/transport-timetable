import { BaseSideService } from "@zeppos/zml/base-side";

AppSideService(
  BaseSideService({
    onInit() {
      this.log("app side service invoke onInit");
    },

    onRun() {
      this.log("app side service invoke onRun");
    },

    onRequest(req, res) {
      const { method, params } = req;

      if (method === "GET_TOKEN") {
        const token = this.settings.getItem("access_token") || "";
        const expiry = this.settings.getItem("token_expiry_time") || "";
        res(null, { token: token, expiry: expiry });
        return;
      }

      if (method === "DOWNLOAD_MAP") {
        this.log(`Received Map request for URL: ${params?.url}`);

        if (!params || !params.url) {
          res(null, { status: "ERROR", message: "Missing URL parameter" });
          return;
        }

        this.downloadMap(
          params.url + "&nonce=" + this.generateRandomLetters(10),
          params.token,
        );
        res(null, { status: "SUCCESS", fileName: "route_map.png" });
        return;
      }

      res(null, { status: "ERROR", message: "Method unsupported" });
    },

    // Helper to generate a 20-character random string of pure letters
    generateRandomLetters(length = 20) {
      const chars = "abcdefghijklmnopqrstuvwxyz";
      let result = "";
      for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
      }
      return result;
    },

    downloadMap(url, token) {
      const randomId = this.generateRandomLetters(15);

      // 2. Keep the core filenames concise
      const sourceName = "map.png";
      const convertedName = `converted_map.png`;
      let task = network.downloader.downloadFile({
        url: url,
        headers: {
          Authorization: `Bearer ${token || ""}`,
        },
        timeout: 60000,
        filePath: `data://download/${sourceName}`,
      });

      const ref = this;

      this.notifyDevice("Started..");

      task.onSuccess = function (data) {
        ref.notifyDevice("Map downloaded...");
        //ref.log("Download completed successfully on side-service handler.");
        // Fire file sync instantly when pipeline confirms write lock closed
        ref.convertImage(data.filePath, convertedName, () => {
          //ref.log("Image conversion callback executed, proceeding to file transfer.");
          ref.notifyDevice("Conversion complete...");
          ref.transferFileToDevice(convertedName);
        });
      };

      task.onFail = function (data) {
        ref.log("Download task handling error pipeline failure.");
      };

      task.onComplete = function () {
        // Left empty to prevent duplicated double-transfer callbacks triggering
      };

      task.onProgress = function (data) {
        ref.notifyDevice("Map downloading...");
      };

      return task;
    },

    notifyDevice(message) {
      this.call({
        method: "File",
        message: message,
      });
    },

    transferFileToDevice(convertedName) {
      this.log(
        "Initiating file bridge stream transfer across BLE connection...",
      );
      const task = this.sendFile(`data://download/${convertedName}`, {
        type: "image",
      });

      let ref = this;

      task.on("progress", (e) => {});
      task.on("change", (e) => {
        if (e.data.readyState === "transferred") {
          ref.notifyDevice("Map transfer complete...");
        }
      });

      this.notifyDevice("Map transfer started...");
      return task;
    },

    async convertImage(sourceName, convertedName, cb) {
      this.notifyDevice("Conversion in progress..");
      const result = await this.convert({
        filePath: `${sourceName}`,
        targetFilePath: `data://download/${convertedName}`,
      });

      cb();
    },

    onDestroy() {
      this.log("app side service invoke onDestroy");
    },
  }),
);
