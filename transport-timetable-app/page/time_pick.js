// time_pick.js
import {createWidget, setStatusBarVisible, widget} from "@zos/ui";
import { Time } from "@zos/sensor";
import { back } from "@zos/router";
import { getDeviceInfo } from "@zos/device";
import { BasePage } from "@zeppos/zml/base-page";

Page(
    BasePage({
      build() {
        setStatusBarVisible(false);
        const { width: SCREEN_W } = getDeviceInfo();
        const time = new Time();
        let tempHour = time.getHours();
        let tempMinute = time.getMinutes();

        // Dynamically calculate picker column width based on screen width
        const colWidth = Math.max(80, Math.round((SCREEN_W * 0.44) / 2));

        createWidget(widget.WIDGET_PICKER, {
          title: "Select Time",
          nb_of_columns: 2,
          single_wide: true,
          init_col_index: 0,
          data_config: [
            {
              data_array: Array.from({ length: 24 }, (_, i) => i),
              init_val_index: tempHour,
              unit: "H",
              support_loop: true,
              font_size: 22,
              select_font_size: 34,
              col_width: colWidth,
            },
            {
              data_array: Array.from({ length: 60 }, (_, i) => i),
              init_val_index: tempMinute,
              unit: "Min",
              support_loop: true,
              font_size: 22,
              select_font_size: 34,
              col_width: colWidth,
            },
          ],
          picker_cb: (picker, event_type, column_index, select_index) => {
            if (event_type === 1) {
              if (column_index === 0) tempHour = select_index;
              if (column_index === 1) tempMinute = select_index;
            } else if (event_type === 2) {
              const hh = String(tempHour).padStart(2, "0");
              const min = String(tempMinute).padStart(2, "0");

              const app = getApp();
              if (!app.globalData) app.globalData = {};
              app.globalData.routeTime = `${hh}:${min}`;
              back(); // Safely pops view context back to previous page
            } else if (event_type === 3) {
              back();
            }
          },
        });
      },
    }),
);
