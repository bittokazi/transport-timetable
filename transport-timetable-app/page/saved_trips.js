// page/saved_trips.js
import hmUI, { setStatusBarVisible } from "@zos/ui";
import { widget, align, text_style, event } from "@zos/ui";
import { log } from "@zos/utils";
import { getDeviceInfo } from "@zos/device";
import { BasePage } from "@zeppos/zml/base-page";
import { back, push, replace } from "@zos/router";
import { LocalStorage } from "@zos/storage";
import { showToast } from "@zos/interaction";

const SAVED_TRIPS_KEY = "saved_trips";

function getStorage() {
    return new LocalStorage();
}

function parseDateSafe(isoString) {
    if (!isoString) return null;
    try {
        const d = new Date(isoString);
        return Number.isNaN(d.getTime()) ? null : d;
    } catch (e) {
        return null;
    }
}

function formatTime(isoString) {
    const d = parseDateSafe(isoString);
    if (!d) return "--:--";
    const hours = String(d.getHours()).padStart(2, "0");
    const minutes = String(d.getMinutes()).padStart(2, "0");
    return `${hours}:${minutes}`;
}

function formatTripDate(trip, fallbackIso) {
    const d =
        parseDateSafe(trip?.start) ||
        parseDateSafe(trip?.end) ||
        parseDateSafe(fallbackIso);
    if (!d) return "";
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function getDuration(startIso, endIso) {
    const start = parseDateSafe(startIso);
    const end = parseDateSafe(endIso);
    if (!start || !end) return "";
    const diffMins = Math.round((end - start) / 60000);
    return `${diffMins} min`;
}

function isPastTrip(trip) {
    if (!trip) return false;
    const d = parseDateSafe(trip.end) || parseDateSafe(trip.start);
    if (!d) return false;
    return d.getTime() < Date.now();
}

Page(
    BasePage({
        state: {
            savedTrips: [],
            isDeleting: false,
        },

        onInit() {
            this.state.savedTrips = this.loadSavedTrips();
            this.state.isDeleting = false;
        },

        loadSavedTrips() {
            try {
                const raw = getStorage().getItem(SAVED_TRIPS_KEY);
                if (!raw) return [];

                const parsed = typeof raw === "string" ? JSON.parse(raw) : raw;
                if (!Array.isArray(parsed)) return [];

                return parsed.sort((a, b) => {
                    const timeA = a?.savedAt ? new Date(a.savedAt).getTime() : 0;
                    const timeB = b?.savedAt ? new Date(b.savedAt).getTime() : 0;
                    return timeB - timeA;
                });
            } catch (e) {
                log.error("Failed to load saved trips:", e);
                return [];
            }
        },

        persistSavedTrips(list) {
            try {
                getStorage().setItem(SAVED_TRIPS_KEY, JSON.stringify(list));
                this.state.savedTrips = list;
            } catch (e) {
                log.error("Failed to persist saved trips:", e);
            }
        },

        deleteSavedTrip(targetEntry, targetIndex) {
            const currentList = this.loadSavedTrips();
            const updated = currentList.filter((item, idx) => {
                if (targetEntry?.savedId && item?.savedId) {
                    return item.savedId !== targetEntry.savedId;
                }
                return idx !== targetIndex;
            });
            this.persistSavedTrips(updated);
        },

        openSavedTrip(entry) {
            if (this.state.isDeleting || !entry || !entry.trip) return;
            push({
                url: "page/route",
                params: {
                    trip: entry.trip,
                    from: entry.from,
                    to: entry.to,
                    fromSaved: true,
                },
            });
        },

        build() {
            setStatusBarVisible(false);
            const refSelf = this;
            const { width: SCREEN_W, height: SCREEN_H } = getDeviceInfo();

            const trips = this.loadSavedTrips();
            this.state.savedTrips = trips;
            this.state.isDeleting = false;

            const marginX = Math.round(SCREEN_W * 0.08);
            const usableW = SCREEN_W - marginX * 2;
            const headerTopY = Math.round(SCREEN_H * 0.08);
            const headerTitleH = Math.round(SCREEN_H * 0.085);
            const subtitleGap = Math.round(SCREEN_H * 0.01);
            const subtitleH = Math.round(SCREEN_H * 0.055);
            const listStartY =
                headerTopY +
                headerTitleH +
                subtitleGap +
                subtitleH +
                Math.round(SCREEN_H * 0.03);

            const titleSize = Math.max(18, Math.round(SCREEN_W * 0.056));
            const subtitleSize = Math.max(13, Math.round(SCREEN_W * 0.036));

            hmUI.createWidget(widget.TEXT, {
                x: marginX,
                y: headerTopY,
                w: usableW,
                h: headerTitleH,
                text: "Saved Trips",
                text_size: titleSize,
                color: 0xffffff,
                align_h: align.CENTER_H,
                align_v: align.CENTER_V,
            });

            const countWidget = hmUI.createWidget(widget.TEXT, {
                x: marginX,
                y: headerTopY + headerTitleH + subtitleGap,
                w: usableW,
                h: subtitleH,
                text:
                    trips.length === 1
                        ? "1 saved route"
                        : `${trips.length} saved routes`,
                text_size: subtitleSize,
                color: 0x00e676,
                align_h: align.CENTER_H,
                align_v: align.CENTER_V,
            });

            let currentY = listStartY;

            const cardGap = Math.round(SCREEN_H * 0.025);
            const cardH = Math.round(SCREEN_H * 0.26);
            const cardRadius = Math.round(cardH * 0.16);
            const innerPadX = Math.round(usableW * 0.045);
            const innerPadY = Math.round(cardH * 0.09);

            const delBtnW = Math.round(usableW * 0.18);
            const delBtnGap = Math.round(usableW * 0.025);
            const infoAreaW = usableW - delBtnW - delBtnGap;

            const routeFontSize = Math.max(15, Math.round(SCREEN_W * 0.043));
            const scheduleFontSize = Math.max(13, Math.round(SCREEN_W * 0.035));
            const dateMetaFontSize = Math.max(11, Math.round(SCREEN_W * 0.031));
            const delFontSize = Math.max(13, Math.round(SCREEN_W * 0.036));
            const actionBtnFontSize = Math.max(16, Math.round(SCREEN_W * 0.046));

            if (!trips.length) {
                const emptyBoxH = Math.round(SCREEN_H * 0.24);
                hmUI.createWidget(widget.FILL_RECT, {
                    x: marginX,
                    y: currentY,
                    w: usableW,
                    h: emptyBoxH,
                    radius: cardRadius,
                    color: 0x161b22,
                });

                hmUI.createWidget(widget.TEXT, {
                    x: marginX + innerPadX,
                    y: currentY + Math.round(emptyBoxH * 0.18),
                    w: usableW - innerPadX * 2,
                    h: Math.round(emptyBoxH * 0.34),
                    text: "No saved trips yet",
                    text_size: routeFontSize,
                    color: 0xffffff,
                    align_h: align.CENTER_H,
                    align_v: align.CENTER_V,
                });

                hmUI.createWidget(widget.TEXT, {
                    x: marginX + innerPadX,
                    y: currentY + Math.round(emptyBoxH * 0.52),
                    w: usableW - innerPadX * 2,
                    h: Math.round(emptyBoxH * 0.32),
                    text: "Tap Save Trip on any route",
                    text_size: scheduleFontSize,
                    color: 0x8b949e,
                    align_h: align.CENTER_H,
                    align_v: align.CENTER_V,
                });

                currentY += emptyBoxH + Math.round(SCREEN_H * 0.04);
            } else {
                trips.forEach((entry, index) => {
                    const cardWidgets = [];

                    const fromName =
                        entry?.from?.name ||
                        entry?.trip?.legs?.[0]?.from?.name ||
                        "Origin";
                    const toName =
                        entry?.to?.name ||
                        entry?.trip?.legs?.[entry?.trip?.legs?.length - 1]?.to?.name ||
                        "Destination";

                    const tripStart = formatTime(entry?.trip?.start);
                    const tripEnd = formatTime(entry?.trip?.end);
                    const tripDuration = getDuration(entry?.trip?.start, entry?.trip?.end);
                    const legsCount = Array.isArray(entry?.trip?.legs)
                        ? entry.trip.legs.length
                        : 0;

                    const scheduleLine = tripDuration
                        ? `${tripStart} - ${tripEnd} (${tripDuration})`
                        : `${legsCount} ${legsCount === 1 ? "leg" : "legs"}`;

                    const past = isPastTrip(entry?.trip);
                    const tripDateStr = formatTripDate(entry?.trip, entry?.savedAt);
                    const dateLine = past
                        ? tripDateStr
                            ? `Past trip • ${tripDateStr}`
                            : "Past trip"
                        : tripDateStr || "Scheduled trip";

                    cardWidgets.push(
                        hmUI.createWidget(widget.FILL_RECT, {
                            x: marginX,
                            y: currentY,
                            w: infoAreaW,
                            h: cardH,
                            radius: cardRadius,
                            color: 0x16202a,
                        })
                    );

                    const accentW = Math.max(3, Math.round(SCREEN_W * 0.01));
                    const accentH = Math.round(cardH * 0.64);
                    cardWidgets.push(
                        hmUI.createWidget(widget.FILL_RECT, {
                            x: marginX + Math.round(innerPadX * 0.55),
                            y: currentY + Math.round((cardH - accentH) / 2),
                            w: accentW,
                            h: accentH,
                            radius: Math.round(accentW / 2),
                            color: past ? 0xffa726 : 0x00e676,
                        })
                    );

                    const textStartX = marginX + innerPadX + accentW;
                    const textW = infoAreaW - innerPadX * 1.5 - accentW;
                    const row1H = Math.round(cardH * 0.3);
                    const row2H = Math.round(cardH * 0.25);
                    const row3H = Math.round(cardH * 0.25);

                    cardWidgets.push(
                        hmUI.createWidget(widget.TEXT, {
                            x: textStartX,
                            y: currentY + innerPadY,
                            w: textW,
                            h: row1H,
                            text: `${fromName} → ${toName}`,
                            text_size: routeFontSize,
                            color: 0xffffff,
                            align_h: align.LEFT,
                            align_v: align.CENTER_V,
                            text_style: text_style.MARQUEE,
                        })
                    );

                    cardWidgets.push(
                        hmUI.createWidget(widget.TEXT, {
                            x: textStartX,
                            y: currentY + innerPadY + row1H,
                            w: textW,
                            h: row2H,
                            text: scheduleLine,
                            text_size: scheduleFontSize,
                            color: past ? 0xffa726 : 0x00e676,
                            align_h: align.LEFT,
                            align_v: align.CENTER_V,
                            text_style: text_style.ELLIPSIS,
                        })
                    );

                    cardWidgets.push(
                        hmUI.createWidget(widget.TEXT, {
                            x: textStartX,
                            y: currentY + innerPadY + row1H + row2H,
                            w: textW,
                            h: row3H,
                            text: dateLine,
                            text_size: dateMetaFontSize,
                            color: past ? 0xffb74d : 0x9aa5b1,
                            align_h: align.LEFT,
                            align_v: align.CENTER_V,
                            text_style: text_style.ELLIPSIS,
                        })
                    );

                    const cardTapTarget = hmUI.createWidget(widget.TEXT, {
                        x: marginX,
                        y: currentY,
                        w: infoAreaW,
                        h: cardH,
                        text: "",
                    });
                    if (cardTapTarget) {
                        cardTapTarget.addEventListener(event.CLICK_DOWN, () => {
                            refSelf.openSavedTrip(entry);
                        });
                        cardWidgets.push(cardTapTarget);
                    }

                    const delBtn = hmUI.createWidget(widget.BUTTON, {
                        x: marginX + infoAreaW + delBtnGap,
                        y: currentY,
                        w: delBtnW,
                        h: cardH,
                        radius: cardRadius,
                        normal_color: 0x3b1818,
                        press_color: 0x5c2323,
                        text: "Del",
                        text_size: delFontSize,
                        color: 0xff6b6b,
                        align_h: align.CENTER_H,
                        align_v: align.CENTER_V,
                        click_func: () => {
                            if (refSelf.state.isDeleting) return;
                            refSelf.state.isDeleting = true;

                            delBtn.setProperty(hmUI.prop.MORE, { text: "..." });
                            refSelf.deleteSavedTrip(entry, index);

                            cardWidgets.forEach((w) => {
                                if (w && typeof w.setProperty === "function") {
                                    try {
                                        w.setProperty(hmUI.prop.VISIBLE, false);
                                    } catch (e) {
                                        // ignore visibility error
                                    }
                                }
                            });

                            const remaining = refSelf.state.savedTrips.length;
                            countWidget.setProperty(hmUI.prop.MORE, {
                                text:
                                    remaining === 1
                                        ? "1 saved route"
                                        : `${remaining} saved routes`,
                            });

                            showToast({ content: "Deleted Trip" });

                            setTimeout(() => {
                                replace({ url: "page/saved_trips" });
                            }, 120);
                        },
                    });

                    cardWidgets.push(delBtn);

                    currentY += cardH + cardGap;
                });

                currentY += Math.round(SCREEN_H * 0.015);
            }

            // Single Go Back Button
            const backBtnW = Math.round(usableW * 0.72);
            const backBtnX = Math.round((SCREEN_W - backBtnW) / 2);
            const backBtnH = Math.round(SCREEN_H * 0.125);

            hmUI.createWidget(widget.BUTTON, {
                x: backBtnX,
                y: currentY,
                w: backBtnW,
                h: backBtnH,
                radius: Math.round(backBtnH / 2),
                normal_color: 0x2a2f36,
                press_color: 0x3d444e,
                text: "Go Back",
                text_size: actionBtnFontSize,
                color: 0xffffff,
                click_func: () => {
                    if (refSelf.state.isDeleting) return;
                    back();
                },
            });

            // Bottom circular-screen scroll padding spacer
            hmUI.createWidget(widget.TEXT, {
                x: 0,
                y: currentY + backBtnH + Math.round(SCREEN_H * 0.03),
                w: SCREEN_W,
                h: Math.round(SCREEN_H * 0.18),
                text: "",
            });
        },
    })
);
