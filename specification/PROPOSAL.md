# Proposal

## Pricing with formula

See `zoom-variable-price.yml`

## Custom billing

Que calendario utilizas
Cada cuanto lo quieres cobrar

```yaml
billing: 1B+
```

Y yearly
M monthly
H half year
W weekly

### References

- [Soft dates](https://help.certinia.com/main/2024.2/Content/BillingCentral/Features/SoftDates/SoftDates.htm)
- [Stripe](https://docs.stripe.com/billing/subscriptions/billing-cycle)
- [Calculating Billing Periods and Billing Dates](https://help.certinia.com/main/2024.2/Content/BillingCentral/Features/SoftDates/CalculatingBillingPeriodsAndBillingDates.htm)

## Templates

```yaml
features:
	feature1:
		description: Description 1
		extends: builtin
	feature2:
		description: Description 2
		extends: builtin
usageLimits:
	usageLimit1:
		description: Usage limit 1
		extends: off
templates:
	builtin:
		type: DOMAIN
		useTemplate: on
	on:
		valueType: BOOLEAN
		defaultValue: true
	off:
		valueType: BOOLEAN
		defaultValue: false
```

## Bundles

```yaml
plans:
  BASIC:
  # ...
  PRO:
  # ...
  PREMIUM:
  # ---
addOns:
  A:
  # ...
  B:
  # ...
  C:
  # ...
bundles:
  bundle1:
    - BASIC
    - A
  bundle2:
    - PRO
    - A
    - B
  bundle3:
    - PREMIUM
    - A
    - B
    - C
```
