package ch.umb.curo.starter.models.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.camunda.bpm.engine.variable.value.FileValue

@JsonInclude(JsonInclude.Include.NON_NULL)
class CuroFileVariable {

    /**
     * Name of the file
     **/
    @Schema(description = "Name of the file")
    var fileName: String? = null

    /**
     * MimeType of the file
     **/
    @Schema(description = "MimeType of the file")
    var mimeType: String? = null

    /**
     * Encoding of the file
     **/
    @Schema(description = "Encoding of the file")
    var encoding: String? = null

    companion object {
        fun fromFileValue(value: FileValue): CuroFileVariable {
            val curoFileVariable = CuroFileVariable()
            curoFileVariable.fileName = value.filename
            curoFileVariable.mimeType = value.mimeType
            curoFileVariable.encoding = value.encoding
            return curoFileVariable
        }
    }

}
