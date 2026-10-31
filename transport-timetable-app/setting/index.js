// settings/index.js
import appConfig from "./../common/config";
import { APP_VERSION, BUILD_VERSION } from "../common/version";

AppSettingsPage({
    state: {
        isLoggedIn: false,
    },

    checkTokenValidity(props) {
        const accessToken = props.settingsStorage.getItem("access_token");
        const expiryTime = props.settingsStorage.getItem("token_expiry_time");

        if (accessToken && expiryTime) {
            const currentTime = Date.now();
            if (currentTime < Number(expiryTime)) {
                return true;
            }
        }
        return false;
    },

    build(props) {
        const isTokenValid = this.checkTokenValidity(props);
        console.log("Token Valid:", isTokenValid);

        // Helper style for nice section titles
        const sectionTitleStyle = {
            fontSize: "14px",
            fontWeight: "600",
            color: "#6e6e73",
            textTransform: "uppercase",
            letterSpacing: "0.5px",
            marginBottom: "8px",
            marginTop: "20px",
            paddingLeft: "4px",
            display: "block",
            width: "100%",
        };

        return View(
            {
                style: {
                    padding: "16px",
                    backgroundColor: "#f4f5f7",
                    minHeight: "100vh",
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "space-between",
                },
            },
            [
                View({}, [
                    // Section 1 Title & Info Banner
                    Text({ style: sectionTitleStyle }, ["Manage Account"]),
                    View(
                        {
                            style: {
                                padding: "12px 16px",
                                backgroundColor: "#e9f2ff",
                                borderRadius: "10px",
                                marginBottom: "12px",
                                borderLeft: "4px solid #007AFF",
                            },
                        },
                        [
                            Text(
                                {
                                    style: {
                                        color: "#0040dd",
                                        fontSize: "13px",
                                        lineHeight: "1.4",
                                        display: "block",
                                        width: "100%",
                                    },
                                },
                                ["To create or log in to your account, please click the Management & Portal button below."]
                            ),
                        ]
                    ),

                    Link(
                        { source: `${appConfig.url}/app/login` },
                        [
                            View(
                                {
                                    style: {
                                        padding: "12px 16px",
                                        backgroundColor: "#ffffff",
                                        borderRadius: "12px",
                                        marginBottom: "12px",
                                        boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
                                    },
                                },
                                [
                                    Text(
                                        {
                                            style: {
                                                color: "#007AFF",
                                                fontSize: "16px",
                                                fontWeight: "500",
                                            },
                                        },
                                        ["Account Management & Portal"]
                                    ),
                                ]
                            ),
                        ]
                    ),

                    // Section 2 Title & Info Banner
                    Text({ style: sectionTitleStyle }, ["Watch Authorization"]),
                    View(
                        {
                            style: {
                                padding: "12px 16px",
                                backgroundColor: "#eef9f2",
                                borderRadius: "10px",
                                marginBottom: "12px",
                                borderLeft: "4px solid #34c759",
                            },
                        },
                        [
                            Text(
                                {
                                    style: {
                                        color: "#1b5e30",
                                        fontSize: "13px",
                                        lineHeight: "1.4",
                                        display: "block",
                                        width: "100%",
                                    },
                                },
                                ["To sync your watch with your authenticated account, click the connect button below."]
                            ),
                        ]
                    ),

                    View(
                        {
                            style: {
                                padding: "16px",
                                backgroundColor: "#ffffff",
                                borderRadius: "12px",
                                boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
                            },
                        },
                        [
                            isTokenValid
                                ? Button({
                                    label: "Disconnect Account (Log Out)",
                                    style: {
                                        backgroundColor: "#ff3b30",
                                        color: "#ffffff",
                                        borderRadius: "8px",
                                        padding: "12px",
                                        fontWeight: "600",
                                        border: "none",
                                    },
                                    onClick: () => {
                                        props.settingsStorage.removeItem("access_token");
                                        props.settingsStorage.removeItem("token_expiry_time");
                                    },
                                })
                                : Auth({
                                    title: "Transport Timetable Sync",
                                    label: "Connect Watch Account",
                                    authorizeUrl: `${appConfig.url}/oauth/authorize`,
                                    requestTokenUrl: `${appConfig.url}/oauth/token`,
                                    clientId: appConfig.clientId,
                                    scope: "openid profile email",
                                    pkce: true,
                                    onAccessToken: (data) => {
                                        if (data && data.access_token) {
                                            props.settingsStorage.setItem(
                                                "access_token",
                                                data.access_token
                                            );
                                            const expiresInSeconds = data.expires_in || 3600;
                                            const expiryTimestamp =
                                                Date.now() + expiresInSeconds * 1000;
                                            props.settingsStorage.setItem(
                                                "token_expiry_time",
                                                expiryTimestamp.toString()
                                            );
                                        }
                                    },
                                }),
                        ]
                    ),
                ]),

                // Footer displaying both app version and backend build version, centered
                View(
                    {
                        style: {
                            padding: "24px 0 12px 0",
                            textAlign: "center",
                            width: "100%",
                        },
                    },
                    [
                        Text(
                            {
                                style: {
                                    color: "#8e8e93",
                                    fontSize: "13px",
                                    marginBottom: "4px",
                                    display: "block",
                                    textAlign: "center",
                                    width: "100%",
                                },
                            },
                            [`Transport Timetable • v${APP_VERSION}`]
                        ),
                        Text(
                            {
                                style: {
                                    color: "#aeaeb2",
                                    fontSize: "11px",
                                    display: "block",
                                    textAlign: "center",
                                    width: "100%",
                                },
                            },
                            [`Build Version • ${BUILD_VERSION}`]
                        ),
                    ]
                ),
            ]
        );
    },
});
