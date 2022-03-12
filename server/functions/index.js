const functions = require("firebase-functions");
const admin = require("firebase-admin");
// admin.initializeApp();

const {fetchSource, decodeSource} = require('./loader');

const makeDataRequest = async () => {
    console.info("Downloading data from Paraulògic...");
    const source = await fetchSource();
    console.info("Parsing data...")
    return decodeSource(source);
};

// Redirects to the Github Page
exports.gh = functions.https
    .onRequest(async (_request, response) => response.redirect("https://github.com/ArnyminerZ/Paraulogic-Android"));

// Returns the data the server loads from Paraulògic (only for dev)
exports.fetchSource = functions.https
    .onRequest(async (_request, response) => {
        if(!process.env.FUNCTIONS_EMULATOR)
            return response.send({"error":"not-dev-environment"});

        const gameInfo = await makeDataRequest();
        response.send(gameInfo);
    });

exports.scheduledFunctionCrontab = functions.pubsub.schedule('0 5 * * *')
    .timeZone('Europe/Madrid')
    .onRun(async () => {
        admin.initializeApp();
        const firestore = admin.firestore();

        const gameInfo = makeDataRequest();

        console.info("Creating collection...")
        const collection = firestore.collection("paraulogic");
        await collection.add({
            "timestamp": new Date(),
            "gameInfo": gameInfo,
        });
        console.info('Finished!');

        return null;
    });
