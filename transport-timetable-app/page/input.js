// page/input.js
import { createWidget, widget, align, prop, setStatusBarVisible } from "@zos/ui";
import { BasePage } from "@zeppos/zml/base-page";
import { back } from "@zos/router";
import { LocalStorage } from "@zos/storage";
import { getDeviceInfo } from "@zos/device";
import appConfig from "./../common/config";

const storage = new LocalStorage();

Page(
    BasePage({
      state: {
        searchTimeout: null,
        currentResults: [],
        favoritesList: [],
        selectionType: "FROM",
      },
      onInit(params) {
        this.state.searchTimeout = null;
        this.state.currentResults = [];
        this.state.favoritesList = this.loadFavorites();

        try {
          const parsedParams = typeof params === "string" ? JSON.parse(params) : params;
          if (parsedParams && parsedParams.type) {
            this.state.selectionType = parsedParams.type;
          }
        } catch (e) {
          // fallback
        }
      },
      loadFavorites() {
        try {
          const saved = storage.getItem("favorite_places");
          if (!saved) return [];

          const parsed = JSON.parse(saved);
          return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
          return [];
        }
      },
      fetchData(ref, query, cb) {
        if (this.state.searchTimeout) {
          clearTimeout(this.state.searchTimeout);
          this.state.searchTimeout = null;
        }

        if (!query || !query.trim()) {
          this.state.currentResults = [];
          cb([]);
          return;
        }

        this.state.searchTimeout = setTimeout(() => {
          const app = getApp();
          const token =
              app.globalData && app.globalData["accessToken"]
                  ? app.globalData["accessToken"]
                  : "";

          this.httpRequest({
            method: "GET",
            url: `${appConfig.url}/api/v1/transport/search?query=${encodeURIComponent(query)}`,
            headers: {
              Authorization: `Bearer ${token}`,
              Accept: "application/json",
            },
          })
              .then((result) => {
                const responseBody =
                    typeof result.body === "string"
                        ? JSON.parse(result.body)
                        : result.body;
                this.state.currentResults = Array.isArray(responseBody) ? responseBody : [];
                cb(this.state.currentResults);
              })
              .catch(() => {
                this.state.currentResults = [];
                cb([]);
              });
        }, 300);
      },
      build() {
        setStatusBarVisible(false);
        const refPage = this;
        let inputText = "";
        let searchScrollOffset = 0;
        let favScrollOffset = 0;
        let isNumericMode = false;

        let dynamicWidgets = [];
        let keyboardWidgets = [];
        let currentSuggestionsData = [];

        // Retrieve dynamic device dimensions
        const { width: SCREEN_W, height: SCREEN_H } = getDeviceInfo();
        const VISIBLE_ROWS = 3;
        const ITEM_H = 40;
        const BTN_H = 36;

        // Responsive layout calculations (snapped to exact item rows)
        const LIST_TOP = Math.round(SCREEN_H * 0.13);
        const LIST_H = VISIBLE_ROWS * ITEM_H - 4;
        const LIST_LEFT = Math.round(SCREEN_W * 0.11);
        const LIST_W = SCREEN_W - LIST_LEFT - Math.round(SCREEN_W * 0.16);
        const SCROLL_BTN_X = SCREEN_W - Math.round(SCREEN_W * 0.18);

        const inputWidget = createWidget(widget.TEXT, {
          x: Math.round(SCREEN_W * 0.08),
          y: Math.round(SCREEN_H * 0.02),
          w: SCREEN_W - Math.round(SCREEN_W * 0.16),
          h: 36,
          text_size: 20,
          color: 0xffffff,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text: "Type location...",
        });

        function clearDynamicWidgets() {
          dynamicWidgets.forEach((widgetItem) => {
            if (widgetItem && typeof widgetItem.remove === "function") {
              widgetItem.remove();
            }
          });
          dynamicWidgets = [];
        }

        function clearKeyboardWidgets() {
          keyboardWidgets.forEach((widgetItem) => {
            if (widgetItem && typeof widgetItem.remove === "function") {
              widgetItem.remove();
            }
          });
          keyboardWidgets = [];
        }

        function assignSelectedPlace(place) {
          const app = getApp();
          if (!app.globalData) app.globalData = {};

          if (place) {
            if (refPage.state.selectionType === "TO") {
              app.globalData.toSelection = place;
            } else {
              app.globalData.fromSelection = place;
            }
          }
          back();
        }

        function renderContent() {
          clearDynamicWidgets();

          const bgClear = createWidget(widget.FILL_RECT, {
            x: LIST_LEFT,
            y: LIST_TOP,
            w: LIST_W,
            h: LIST_H,
            color: 0x000000,
          });
          if (bgClear) dynamicWidgets.push(bgClear);

          const isSearching = inputText.trim().length > 0;

          if (isSearching) {
            if (!currentSuggestionsData.length) {
              const emptyText = createWidget(widget.TEXT, {
                x: LIST_LEFT,
                y: LIST_TOP + 45,
                w: LIST_W,
                h: 30,
                text: "Searching...",
                text_size: 14,
                color: 0x999999,
                align_h: align.CENTER_H,
              });
              if (emptyText) dynamicWidgets.push(emptyText);
              return;
            }

            const maxScroll = Math.max(0, (currentSuggestionsData.length - VISIBLE_ROWS) * ITEM_H);
            if (searchScrollOffset < 0) searchScrollOffset = 0;
            if (searchScrollOffset > maxScroll) searchScrollOffset = maxScroll;

            currentSuggestionsData.forEach((itemObj, i) => {
              const baseY = LIST_TOP + i * ITEM_H;
              const y = baseY - searchScrollOffset;

              if (y < LIST_TOP || y + BTN_H > LIST_TOP + LIST_H) return;

              const resultButton = createWidget(widget.BUTTON, {
                x: LIST_LEFT,
                y,
                w: LIST_W,
                h: BTN_H,
                radius: 8,
                text: itemObj.name,
                text_size: 14,
                normal_color: 0x1d2b3a,
                press_color: 0x2f4866,
                click_func: () => {
                  const payload = itemObj.rawPayload || itemObj;
                  assignSelectedPlace(payload);
                },
              });

              if (resultButton) dynamicWidgets.push(resultButton);
            });

            if (maxScroll > 0) {
              const upBtn = createWidget(widget.BUTTON, {
                x: SCROLL_BTN_X,
                y: LIST_TOP,
                w: Math.round(SCREEN_W * 0.1),
                h: BTN_H,
                radius: 6,
                text: "▲",
                text_size: 16,
                normal_color: 0x333333,
                press_color: 0x555555,
                click_func: () => {
                  searchScrollOffset = Math.max(0, searchScrollOffset - ITEM_H);
                  renderContent();
                },
              });
              const downBtn = createWidget(widget.BUTTON, {
                x: SCROLL_BTN_X,
                y: LIST_TOP + LIST_H - BTN_H,
                w: Math.round(SCREEN_W * 0.1),
                h: BTN_H,
                radius: 6,
                text: "▼",
                text_size: 16,
                normal_color: 0x333333,
                press_color: 0x555555,
                click_func: () => {
                  searchScrollOffset = Math.min(maxScroll, searchScrollOffset + ITEM_H);
                  renderContent();
                },
              });
              if (upBtn) dynamicWidgets.push(upBtn);
              if (downBtn) dynamicWidgets.push(downBtn);
            }
          } else {
            const favorites = refPage.loadFavorites();
            if (!favorites.length) {
              const emptyText = createWidget(widget.TEXT, {
                x: LIST_LEFT,
                y: LIST_TOP + 45,
                w: LIST_W,
                h: 30,
                text: "No favorite places saved",
                text_size: 14,
                color: 0x777777,
                align_h: align.CENTER_H,
              });
              if (emptyText) dynamicWidgets.push(emptyText);
              return;
            }

            const maxScroll = Math.max(0, (favorites.length - VISIBLE_ROWS) * ITEM_H);
            if (favScrollOffset < 0) favScrollOffset = 0;
            if (favScrollOffset > maxScroll) favScrollOffset = maxScroll;

            favorites.forEach((favorite, index) => {
              const baseY = LIST_TOP + index * ITEM_H;
              const y = baseY - favScrollOffset;

              if (y < LIST_TOP || y + BTN_H > LIST_TOP + LIST_H) return;

              const selectButton = createWidget(widget.BUTTON, {
                x: LIST_LEFT,
                y,
                w: LIST_W,
                h: BTN_H,
                radius: 8,
                text: favorite.name,
                text_size: 14,
                normal_color: 0x2a2a2a,
                press_color: 0x404040,
                click_func: () => {
                  assignSelectedPlace(favorite);
                },
              });

              if (selectButton) dynamicWidgets.push(selectButton);
            });

            if (maxScroll > 0) {
              const upBtn = createWidget(widget.BUTTON, {
                x: SCROLL_BTN_X,
                y: LIST_TOP,
                w: Math.round(SCREEN_W * 0.1),
                h: BTN_H,
                radius: 6,
                text: "▲",
                text_size: 16,
                normal_color: 0x333333,
                press_color: 0x555555,
                click_func: () => {
                  favScrollOffset = Math.max(0, favScrollOffset - ITEM_H);
                  renderContent();
                },
              });
              const downBtn = createWidget(widget.BUTTON, {
                x: SCROLL_BTN_X,
                y: LIST_TOP + LIST_H - BTN_H,
                w: Math.round(SCREEN_W * 0.1),
                h: BTN_H,
                radius: 6,
                text: "▼",
                text_size: 16,
                normal_color: 0x333333,
                press_color: 0x555555,
                click_func: () => {
                  favScrollOffset = Math.min(maxScroll, favScrollOffset + ITEM_H);
                  renderContent();
                },
              });
              if (upBtn) dynamicWidgets.push(upBtn);
              if (downBtn) dynamicWidgets.push(downBtn);
            }
          }
        }

        function refreshInput() {
          if (inputWidget) {
            inputWidget.setProperty(prop.TEXT, inputText || "Type location...");

            if (!inputText.trim()) {
              searchScrollOffset = 0;
              currentSuggestionsData = [];
              renderContent();
              return;
            }

            refPage.fetchData(refPage, inputText, (data) => {
              currentSuggestionsData = [];
              if (Array.isArray(data)) {
                data.forEach((item) => {
                  if (item && item.name) {
                    currentSuggestionsData.push({
                      name: item.name,
                      rawPayload: item,
                    });
                  }
                });
              }
              renderContent();
            });
          }
        }

        function handleKeyInput(key) {
          inputText += key;
          searchScrollOffset = 0;
          currentSuggestionsData = [];
          renderContent();
          refreshInput();
        }

        // Keyboard sizing and positioning packed cleanly right under the list area
        const keyboardStartY = Math.round(SCREEN_H * 0.45);
        const rowHeight = Math.round(SCREEN_H * 0.075);
        const keyW = Math.floor((SCREEN_W - 48) / 9);
        const keyH = Math.round(rowHeight * 0.85);

        function centerRow(keys) {
          return Math.round((SCREEN_W - (keys.length * keyW + (keys.length - 1) * 3)) / 2);
        }

        function renderKeyboard() {
          clearKeyboardWidgets();

          // Both modes use exactly 9 columns so key widths and alignments match perfectly
          const rows = isNumericMode
              ? [
                { keys: ["1", "2", "3", "4", "5", "6", "7", "8", "9"], y: keyboardStartY },
                { keys: ["0", "-", "/", ":", ".", ",", "@", "(", ")"], y: keyboardStartY + rowHeight },
                { keys: ["ABC", "_", "#", "+", "&", "!", "?", "00", ""], y: keyboardStartY + rowHeight * 2 },
              ]
              : [
                { keys: ["Q", "W", "E", "R", "T", "Y", "U", "I", "O"], y: keyboardStartY },
                { keys: ["P", "A", "S", "D", "F", "G", "H", "J", "K"], y: keyboardStartY + rowHeight },
                { keys: ["?123", "L", "Z", "X", "C", "V", "B", "N", "M"], y: keyboardStartY + rowHeight * 2 },
              ];

          rows.forEach((row) => {
            const startX = centerRow(row.keys);
            row.keys.forEach((key, index) => {
              if (key === "") return; // Skip empty spacer button on numeric row 3

              const isToggle = key === "?123" || key === "ABC";
              const btn = createWidget(widget.BUTTON, {
                x: startX + index * (keyW + 3),
                y: row.y,
                w: keyW,
                h: keyH,
                radius: 6,
                text: key,
                text_size: isToggle ? 10 : 14,
                normal_color: isToggle ? 0x1f3c5c : 0x2d2d2d,
                press_color: isToggle ? 0x2f557f : 0x444444,
                click_func: () => {
                  if (key === "?123") {
                    isNumericMode = true;
                    renderKeyboard();
                  } else if (key === "ABC") {
                    isNumericMode = false;
                    renderKeyboard();
                  } else {
                    handleKeyInput(key);
                  }
                },
              });
              if (btn) keyboardWidgets.push(btn);
            });
          });
        }

        // Control Row (BACK, SPACE, DEL) positioned safely below the 3 keyboard rows
        const controlRowY = keyboardStartY + rowHeight * 3 + 4;
        const controlH = Math.round(rowHeight * 0.85);

        const btnMargin = Math.round(SCREEN_W * 0.08);
        const usableWidth = SCREEN_W - btnMargin * 2;
        const backW = Math.round(usableWidth * 0.22);
        const delW = Math.round(usableWidth * 0.22);
        const spaceW = usableWidth - backW - delW - 8;
        const spaceX = btnMargin + backW + 4;
        const delX = spaceX + spaceW + 4;

        createWidget(widget.BUTTON, {
          x: btnMargin,
          y: controlRowY,
          w: backW,
          h: controlH,
          radius: 6,
          text: "BACK",
          text_size: 12,
          normal_color: 0x333333,
          press_color: 0x555555,
          click_func: () => back(),
        });

        createWidget(widget.BUTTON, {
          x: spaceX,
          y: controlRowY,
          w: spaceW,
          h: controlH,
          radius: 6,
          text: "SPACE",
          text_size: 13,
          normal_color: 0x222222,
          press_color: 0x444444,
          click_func: () => handleKeyInput(" "),
        });

        createWidget(widget.BUTTON, {
          x: delX,
          y: controlRowY,
          w: delW,
          h: controlH,
          radius: 6,
          text: "DEL",
          text_size: 12,
          normal_color: 0xaa2222,
          press_color: 0xcc3333,
          click_func: () => {
            inputText = inputText.slice(0, -1);
            searchScrollOffset = 0;
            currentSuggestionsData = [];
            renderContent();
            refreshInput();
          },
        });

        renderKeyboard();
        renderContent();
      },
    }),
);
