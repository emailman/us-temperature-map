const fs = require('fs');
const path = require('path');

const API_KEY = process.env.OPENWEATHERMAP_API_KEY;
if (!API_KEY) {
    console.error('OPENWEATHERMAP_API_KEY environment variable is required');
    process.exit(1);
}

const CITIES_PATH = path.join(__dirname, '..', 'composeApp', 'src', 'commonMain', 'composeResources', 'files', 'us-cities.json');
const OUTPUT_PATH = path.join(__dirname, '..', 'dist', 'temperatures.json');
const DELAY_MS = 1100;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function fetchWeather(city) {
    const url = `https://api.openweathermap.org/data/2.5/weather?lat=${city.latitude}&lon=${city.longitude}&appid=${API_KEY}&units=imperial`;
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status} for ${city.name}, ${city.state}`);
    }
    return response.json();
}

async function main() {
    const citiesJson = fs.readFileSync(CITIES_PATH, 'utf-8');
    const cities = JSON.parse(citiesJson);
    console.log(`Loaded ${cities.length} cities`);

    const temperatures = [];
    let failed = 0;

    for (let i = 0; i < cities.length; i++) {
        const city = cities[i];
        try {
            const data = await fetchWeather(city);
            const entry = {
                latitude: city.latitude,
                longitude: city.longitude,
                temperature: data.main.temp,
                locationName: data.name,
                cityName: city.name,
                stateName: city.state,
                weatherCondition: data.weather?.[0]?.main ?? '',
                weatherDescription: data.weather?.[0]?.description ?? '',
                humidity: data.main.humidity,
                windSpeed: data.wind?.speed ?? 0,
                windDirection: data.wind?.deg ?? 0,
                tempMin: data.main.temp_min,
                tempMax: data.main.temp_max
            };
            temperatures.push(entry);
            console.log(`[${i + 1}/${cities.length}] ${city.name}, ${city.state}: ${entry.temperature}°F`);
        } catch (err) {
            failed++;
            console.error(`[${i + 1}/${cities.length}] FAILED ${city.name}, ${city.state}: ${err.message}`);
        }

        if (i < cities.length - 1) {
            await sleep(DELAY_MS);
        }
    }

    const failRate = failed / cities.length;
    if (failRate > 0.5) {
        console.error(`Too many failures: ${failed}/${cities.length} (${(failRate * 100).toFixed(0)}%). Aborting.`);
        process.exit(1);
    }

    const now = new Date();
    const pad = n => String(n).padStart(2, '0');
    const fetchedAt = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
    const output = {
        fetchedAt,
        cityCount: temperatures.length,
        temperatures: temperatures
    };

    const outputDir = path.dirname(OUTPUT_PATH);
    if (!fs.existsSync(outputDir)) {
        fs.mkdirSync(outputDir, { recursive: true });
    }

    fs.writeFileSync(OUTPUT_PATH, JSON.stringify(output, null, 2));
    console.log(`\nWrote ${temperatures.length} temperatures to ${OUTPUT_PATH}`);
    if (failed > 0) {
        console.log(`(${failed} cities failed)`);
    }
}

main().catch(err => {
    console.error('Fatal error:', err);
    process.exit(1);
});
