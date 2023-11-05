package io.github.isagroup.models.featureTypes;

import java.util.List;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.ValueType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment extends Feature{

    public Object getValue(){

        if (this.valueType == ValueType.TEXT) {
            List<PaymentType> allowedPayments = null;
            if (this.value instanceof List<?>) {
                List<?> list = (List<?>) this.value;
                if (!list.isEmpty() && list.get(0) instanceof PaymentType) {
                    allowedPayments = (List<PaymentType>) list;
                }
            }
            if (allowedPayments != null) {
                String result = "";
                for (PaymentType value : allowedPayments) {
                    result += value.toString() + ", ";
                }
                result = result.substring(0, result.length() - 2);
                return result;
            }
            throw new PricingParsingException("Payment value is not a list of PaymentType");
        } 
        return this.value;
    } 
}
