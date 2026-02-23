// Serve temperatures.json from the dist directory for local development
const path = require('path');
const distDir = path.resolve(process.cwd(), 'dist');

if (config.devServer) {
    if (!config.devServer.static) {
        config.devServer.static = [];
    } else if (!Array.isArray(config.devServer.static)) {
        config.devServer.static = [config.devServer.static];
    }
    config.devServer.static.push({
        directory: distDir,
        publicPath: '/'
    });
}
