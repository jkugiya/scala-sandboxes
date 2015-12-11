package jkugiya.nom.models.validator


trait ValidationResult

case object ValidationSuccess extends ValidationResult

case class ValidationError(val objectName: String,
                           val field: String,
                           val rejectedValue: Any,
                           code: String,
                           args: Any*) {
}

object ValidationErrors {
  implicit def toErrors(error: ValidationError): ValidationErrors = ValidationErrors(Seq(error))
}

case class ValidationErrors(errors: Seq[ValidationError])
