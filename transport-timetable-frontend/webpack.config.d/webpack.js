if (config.devServer) {
    config.devServer.client = {
        overlay: false
    };
    config.devServer.hot = true;
    config.devServer.open = false;
    config.devServer.port = 3003;
    config.devServer.historyApiFallback = true;
//    config.devServer.compress = false; // workaround for SSE
    config.devtool = 'eval-cheap-source-map';
    config.devServer.allowedHosts = "all";
} else {
    config.devtool = undefined;
}

config.output = {
    filename: (chunkData) => {
        return chunkData.chunk.name === 'main'
            ? "main.bundle.js"
            : "main.bundle-[name].js";
    },
    publicPath: "/app-static",
    library: "project",
    libraryTarget: "umd",
    globalObject: "this"
};
config.output.path = require('path').resolve(__dirname, "../../../dist/libs")

// disable bundle size warning
config.performance = {
    assetFilter: function (assetFilename) {
      return !assetFilename.endsWith('.js');
    },
};
