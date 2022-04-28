package com.arnyminerz.paraulogic.annotation

import androidx.annotation.IntDef
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_FIREBASE_EXCEPTION
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_NO_SUCH_ELEMENT
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_OK

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
@IntDef(RESULT_OK, RESULT_NO_SUCH_ELEMENT, RESULT_FIREBASE_EXCEPTION)
annotation class LoadError {
    companion object {
        /**
         * @author Arnau Mora
         * @since 20220428
         * No error.
         */
        const val RESULT_OK = 0

        /**
         * @author Arnau Mora
         * @since 20220428
         * A [NoSuchElementException] was thrown. Usually related to an error while parsing the server's
         * data.
         */
        const val RESULT_NO_SUCH_ELEMENT = 1

        /**
         * @author Arnau Mora
         * @since 20220428
         * An error occurred while loading data from Firebase.
         */
        const val RESULT_FIREBASE_EXCEPTION = 2
    }
}
