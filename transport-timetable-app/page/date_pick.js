// date_pick.js
import { createWidget, widget, setStatusBarVisible } from "@zos/ui";
import { back } from "@zos/router";
import { getDeviceInfo } from "@zos/device";
import { BasePage } from "@zeppos/zml/base-page";

Page(
    BasePage({
      build() {
        setStatusBarVisible(false);
        const { width: SCREEN_W } = getDeviceInfo();

        const app = getApp();
        const globalData = app.globalData || {};

        // Parse existing global routeDate if available, otherwise use standard JS Date safely
        let initYear, initMonth, initDay;
        if (globalData.routeDate) {
          const parts = globalData.routeDate.split("-");
          initYear = parseInt(parts[0], 10);
          initMonth = parseInt(parts[1], 10);
          initDay = parseInt(parts[2], 10);
        } else {
          const now = new Date();
          initYear = now.getFullYear();
          initMonth = now.getMonth() + 1; // standard JS Date: Jan=0 -> +1 = 1
          initDay = now.getDate();
        }

        let tempYear = initYear;
        let tempMonth = initMonth;
        let tempDay = initDay;

        // Dynamically calculate column width for 3 columns based on screen width
        const colWidth = Math.max(65, Math.floor((SCREEN_W * 0.88) / 3));

        createWidget(widget.WIDGET_PICKER, {
          title: "Select Date",
          nb_of_columns: 3,
          single_wide: true,
          init_col_index: 0,
          data_config: [
            {
              data_array: Array.from({ length: 100 }, (_, i) => i + 1970),
              init_val_index: tempYear - 1970,
              unit: "Y",
              support_loop: true,
              font_size: 20,
              select_font_size: 32,
              col_width: colWidth,
            },
            {
              data_array: Array.from({ length: 12 }, (_, i) => i + 1),
              init_val_index: tempMonth - 1, // Correct index mapping (November = index 10)
              unit: "M",
              support_loop: true,
              font_size: 20,
              select_font_size: 32,
              col_width: colWidth,
            },
            {
              data_array: Array.from({ length: 31 }, (_, i) => i + 1),
              init_val_index: tempDay - 1,
              unit: "D",
              support_loop: true,
              font_size: 20,
              select_font_size: 32,
              col_width: colWidth,
            },
          ],
          picker_cb: (picker, event_type, column_index, select_index) => {
            if (event_type === 1) {
              if (column_index === 0) tempYear = select_index + 1970;
              if (column_index === 1) tempMonth = select_index + 1;
              if (column_index === 2) tempDay = select_index + 1;
            } else if (event_type === 2) {
              const mm = String(tempMonth).padStart(2, "0");
              const dd = String(tempDay).padStart(2, "0");

              if (!app.globalData) app.globalData = {};
              app.globalData.routeDate = `${tempYear}-${mm}-${dd}`;
              back(); // Safely pops view context back to previous page
            } else if (event_type === 3) {
              back();
            }
          },
        });
      },
    }),
);
