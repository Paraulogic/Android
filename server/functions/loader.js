const req = require('./httpPromise');

module.exports = {
    /**
     * Fetches the source code from the Paraulògic website.
     * @author Arnau Mora
     * @since 20220308
     * @return {Promise<string>}
     */
    fetchSource: async () => (await req("https://vilaweb.cat/paraulogic/")).body,
    /**
     * Decodes the source code of Paraulògic.
     * @author Arnau Mora
     * @since 20220308
     * @param {string} source The loaded source code.
     * @return {{letters:string[],centerLetter:string,words:{}}}
     */
    decodeSource: (source) => {
        const tPos = source.indexOf('var t=');
        const sPos = source.indexOf(';', tPos);
        const data = source.substring(tPos, sPos);

        const lettersPos = data.indexOf('"l":[');
        const lettersEndPos = data.indexOf(']', lettersPos);
        const lettersArray = data.substring(lettersPos + 5, lettersEndPos);
        const splitLetters = lettersArray.split(',');
        /**
         * @type {string[]}
         */
        const letters = [];
        for (let l in splitLetters)
            if (splitLetters.hasOwnProperty(l)){
                const splitLetter = splitLetters[l];
                const quoteStart = splitLetter.indexOf('"') + 1;
                const quoteEnd = splitLetter.indexOf('"', quoteStart);
                let letter = splitLetter
                    .substring(quoteStart, quoteEnd)
                    .trim()
                    .toLowerCase();
                if(letter.startsWith("\\")) {
                    const charCode = letter.substring(2);
                    letter = String.fromCharCode(parseInt(charCode, 16));
                }
                letters.push(letter);
            }

        const wordsPos = data.indexOf('"p":{');
        const wordsEnd = data.indexOf('}', wordsPos);
        const wordsString = data.substring(wordsPos + 5, wordsEnd);
        const splitWords = wordsString.split(',');
        const correctWords = {};
        for (let w in splitWords)
            if (splitWords.hasOwnProperty(w)) {
                const word = splitWords[w];
                const splitWord = word.split(':');
                const key = splitWord[0]
                    .replaceAll('"', "")
                    .trim()
                    .toLowerCase();
                correctWords[key] = splitWord[1]
                    .replaceAll('"', "")
                    .trim()
                    .toLowerCase();
            }

        return {
            "letters": letters.slice(0, letters.length - 1),
            "centerLetter": letters[letters.length - 1],
            words: correctWords,
        }
    }
}