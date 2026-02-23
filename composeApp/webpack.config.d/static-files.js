// Serve temperatures.json from the dist directory for local development
const path = require('path');
const fs = require('fs');

const candidates = [
    path.resolve(process.cwd(), 'dist'),
    path.resolve(process.cwd(), '..', 'dist'),
    path.resolve(__dirname, '..', '..', '..', '..', '..', '..', 'dist'),
    path.resolve(__dirname, '..', '..', '..', '..', '..', 'dist'),
    path.resolve(__dirname, '..', '..', '..', '..', 'dist'),
    path.resolve(__dirname, '..', '..', '..', 'dist'),
    path.resolve(__dirname, '..', '..', 'dist'),
    path.resolve(__dirname, '..', 'dist'),
];

const distDir = candidates.find(function(dir) {
    return fs.existsSync(path.join(dir, 'temperatures.json'));
});

if (distDir && config.devServer) {
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
