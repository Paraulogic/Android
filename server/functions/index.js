const functions = require("firebase-functions");
const admin = require("firebase-admin");
// admin.initializeApp();

const {fetchSource, decodeSource} = require('./loader');

exports.scheduledFunctionCrontab = functions.pubsub.schedule('0 5 * * *')
    .timeZone('Europe/Madrid')
    .onRun(async () => {
        admin.initializeApp();
        const firestore = admin.firestore();

        console.info("Downloading data from Paraulògic...");
        const source = await fetchSource();
        console.info("Parsing data...")
        const gameInfo = decodeSource(source);

        console.info("Creating collection...")
        const collection = firestore.collection("paraulogic");
        await collection.add({
            "timestamp": new Date(),
            "gameInfo": gameInfo,
        });
        console.info('Finished!');

        return null;
    });
