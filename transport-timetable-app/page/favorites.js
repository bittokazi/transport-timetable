// page/favorites.js
import {createWidget, widget, align, prop, setStatusBarVisible} from "@zos/ui";
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
      },
      onInit() {
        this.state.searchTimeout = null;
        this.state.currentResults = [];
        this.state.favoritesList = this.loadFavorites();
      },
      getFavoriteKey(item) {
        if (!item) return "";
        if (item.id) return `id:${String(item.id)}`;

        const lat = Number(item.lat);
        const lon = Number(item.lon);
        if (!Number.isNaN(lat) && !Number.isNaN(lon)) {
          return `coord:${lat.toFixed(5)}:${lon.toFixed(5)}`;
        }

        if (item.name) {
          return `name:${String(item.name).trim().toLowerCase()}`;
        }

        return "";
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
      persistFavorites(list) {
        try {
          storage.setItem("favorite_places", JSON.stringify(list));
          this.state.favoritesList = list;
        } catch (e) {
          // ignore persistence errors
        }
      },
      addFavoriteFromPayload(place) {
        if (!place || !place.name) return false;

        const list = this.loadFavorites();
        const key = this.getFavoriteKey(place);
        if (!key) return false;

        const duplicate = list.some((favorite) => this.getFavoriteKey(favorite) === key);
        if (duplicate) return false;

        const normalized = {
          ...place,
          name: place.name,
          id: place.id || null,
          lat: place.lat ?? null,
          lon: place.lon ?? null,
        };

        list.push(normalized);
        this.persistFavorites(list);
        return true;
      },
      removeFavorite(item) {
        const list = this.loadFavorites();
        const key = this.getFavoriteKey(item);
        if (!key) return;

        const filtered = list.filter((favorite) => this.getFavoriteKey(favorite) !== key);
        this.persistFavorites(filtered);
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

        let dynamicWidgets = [];
        let currentSuggestionsData = [];

        // Retrieve dynamic device screen dimensions
        const { width: SCREEN_W, height: SCREEN_H } = getDeviceInfo();
        const ITEM_H = 40;

        // Responsive layout calculations
        const LIST_TOP = Math.round(SCREEN_H * 0.13);
        const LIST_H = Math.round(SCREEN_H * 0.31);
        const LIST_LEFT = Math.round(SCREEN_W * 0.11);
        const LIST_W = SCREEN_W - LIST_LEFT - Math.round(SCREEN_W * 0.16);
        const SCROLL_BTN_X = SCREEN_W - Math.round(SCREEN_W * 0.18);

        const inputWidget = createWidget(widget.TEXT, {
          x: Math.round(SCREEN_W * 0.08),
          y: Math.round(SCREEN_H * 0.05),
          w: SCREEN_W - Math.round(SCREEN_W * 0.16),
          h: 36,
          text_size: 20,
          color: 0xffffff,
          align_h: align.CENTER_H,
          align_v: align.CENTER_V,
          text: "Type to add favorite...",
        });

        function clearDynamicWidgets() {
          dynamicWidgets.forEach((widgetItem) => {
            if (widgetItem && typeof widgetItem.remove === "function") {
              widgetItem.remove();
            }
          });
          dynamicWidgets = [];
        }

        function renderContent() {
          clearDynamicWidgets();

          // Wipe background to prevent ghosting
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
            // --- RENDER SEARCH RESULTS ---
            if (!currentSuggestionsData.length) {
              const emptyText = createWidget(widget.TEXT, {
                x: LIST_LEFT,
                y: LIST_TOP + 50,
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

            const maxScroll = Math.max(0, currentSuggestionsData.length * ITEM_H - LIST_H);
            if (searchScrollOffset < 0) searchScrollOffset = 0;
            if (searchScrollOffset > maxScroll) searchScrollOffset = maxScroll;

            currentSuggestionsData.forEach((itemObj, i) => {
              const baseY = LIST_TOP + i * ITEM_H;
              const y = baseY - searchScrollOffset;

              if (y < LIST_TOP - 10 || y > LIST_TOP + LIST_H - 10) return;

              const resultButton = createWidget(widget.BUTTON, {
                x: LIST_LEFT,
                y,
                w: LIST_W,
                h: 36,
                radius: 8,
                text: itemObj.name,
                text_size: 14,
                normal_color: 0x1d2b3a,
                press_color: 0x2f4866,
                click_func: () => {
                  const payload = itemObj.rawPayload || itemObj;
                  refPage.addFavoriteFromPayload(payload);
                  inputText = "";
                  searchScrollOffset = 0;
                  currentSuggestionsData = [];
                  if (inputWidget) {
                    inputWidget.setProperty(prop.TEXT, "Type to add favorite...");
                  }
                  renderContent();
                },
              });

              if (resultButton) dynamicWidgets.push(resultButton);
            });

            if (maxScroll > 0) {
              const upBtn = createWidget(widget.BUTTON, {
                x: SCROLL_BTN_X,
                y: LIST_TOP,
                w: Math.round(SCREEN_W * 0.1),
                h: 36,
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
                y: LIST_TOP + LIST_H - 36,
                w: Math.round(SCREEN_W * 0.1),
                h: 36,
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
            // --- RENDER FAVORITES LIST WITH REMOVE BUTTONS ---
            const favorites = refPage.loadFavorites();
            if (!favorites.length) {
              const emptyText = createWidget(widget.TEXT, {
                x: LIST_LEFT,
                y: LIST_TOP + 50,
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

            const maxScroll = Math.max(0, favorites.length * ITEM_H - LIST_H);
            if (favScrollOffset < 0) favScrollOffset = 0;
            if (favScrollOffset > maxScroll) favScrollOffset = maxScroll;

            const selectBtnW = Math.round(LIST_W * 0.68);
            const removeBtnW = LIST_W - selectBtnW - 6;

            favorites.forEach((favorite, index) => {
              const baseY = LIST_TOP + index * ITEM_H;
              const y = baseY - favScrollOffset;

              if (y < LIST_TOP - 10 || y > LIST_TOP + LIST_H - 10) return;

              const selectButton = createWidget(widget.BUTTON, {
                x: LIST_LEFT,
                y,
                w: selectBtnW,
                h: 36,
                radius: 8,
                text: favorite.name,
                text_size: 13,
                normal_color: 0x2a2a2a,
                press_color: 0x404040,
                click_func: () => {},
              });

              const removeButton = createWidget(widget.BUTTON, {
                x: LIST_LEFT + selectBtnW + 6,
                y,
                w: removeBtnW,
                h: 36,
                radius: 8,
                text: "Remove",
                text_size: 12,
                normal_color: 0x5d1d1d,
                press_color: 0x7d2b2b,
                click_func: () => {
                  refPage.removeFavorite(favorite);
                  renderContent();
                },
              });

              if (selectButton) dynamicWidgets.push(selectButton);
              if (removeButton) dynamicWidgets.push(removeButton);
            });

            if (maxScroll > 0) {
              const upBtn = createWidget(widget.BUTTON, {
                x: SCROLL_BTN_X,
                y: LIST_TOP,
                w: Math.round(SCREEN_W * 0.1),
                h: 36,
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
                y: LIST_TOP + LIST_H - 36,
                w: Math.round(SCREEN_W * 0.1),
                h: 36,
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
            inputWidget.setProperty(prop.TEXT, inputText || "Type to add favorite...");

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

        // Keyboard sizing and positioning packed right under the list area
        const keyboardStartY = Math.round(SCREEN_H * 0.47);
        const rowHeight = Math.round(SCREEN_H * 0.082);
        const keyW = Math.floor((SCREEN_W - 48) / 9);
        const keyH = Math.round(rowHeight * 0.82);

        function centerRow(keys) {
          return Math.round((SCREEN_W - (keys.length * keyW + (keys.length - 1) * 3)) / 2);
        }

        const rows = [
          { keys: ["Q", "W", "E", "R", "T", "Y", "U", "I", "O"], y: keyboardStartY },
          { keys: ["P", "A", "S", "D", "F", "G", "H", "J", "K"], y: keyboardStartY + rowHeight },
          { keys: ["L", "Z", "X", "C", "V", "B", "N", "M"], y: keyboardStartY + rowHeight * 2 },
        ];

        rows.forEach((row) => {
          const startX = centerRow(row.keys);
          row.keys.forEach((key, index) => {
            createWidget(widget.BUTTON, {
              x: startX + index * (keyW + 3),
              y: row.y,
              w: keyW,
              h: keyH,
              radius: 6,
              text: key,
              text_size: 15,
              normal_color: 0x2d2d2d,
              press_color: 0x444444,
              click_func: () => handleKeyInput(key),
            });
          });
        });

        // Control Row (BACK, SPACE, DEL) positioned immediately after the character keyboard rows
        const controlRowY = keyboardStartY + rowHeight * 3 + 2;
        const controlH = Math.round(rowHeight * 0.85);

        const btnMargin = Math.round(SCREEN_W * 0.08);
        const usableWidth = SCREEN_W - (btnMargin * 2);
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

        renderContent();
      },
    }),
);
