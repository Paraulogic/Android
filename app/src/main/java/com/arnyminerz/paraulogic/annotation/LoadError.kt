package com.arnyminerz.paraulogic.annotation

import androidx.annotation.IntDef
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_NO_SUCH_ELEMENT
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_OK

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
@IntDef(RESULT_OK, RESULT_NO_SUCH_ELEMENT)
annotation class LoadError {
    companion object {
        /**
         * No error.
         * @author Arnau Mora
         * @since 20220428
         */
        const val RESULT_OK = 0

        /**
         * A [NoSuchElementException] was thrown. Usually related to an error while parsing the server's
         * data.
         * @author Arnau Mora
         * @since 20220428
         */
        const val RESULT_NO_SUCH_ELEMENT = 1
    }
}
