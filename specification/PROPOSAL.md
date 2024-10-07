# Proposal

## Pricing with formula

See `zoom-variable-price.yml`

```yaml
plan:
  PRO:
    price: 6*x + 2
variables:
  x: userContext['seats']
  y: 40
  z: 20
```

Spel tiene api para validar una expresión

## References
- https://zapier.com/pricing

## Custom billing

Dependiendo de a que boton le des se renderiza
los precios

Click

|--------|----------|------------|
| Yearly |  Monthly | Six months |
|--------|----------|------------|
```yaml
billing:
  monthly: 1
  3-month: 0.98
  6-month: 0.95
  yearly: 0.9
plans:
  PRO:
    price: 30
    useMultipleBilling: true
```

```yaml
monthlyPrice: 14.99
annualPrice: 13.49
```
Factor descuento anual:

discount_factor = monthlyPrice / annualPrice

Y si el billing lo metemos en plans?

```yaml
plans:
  billing:
    monthly: 1
    3-month: 0.98
    6-month: 0.95
    yearly: 0.9
  plans:
    BASIC:
      price: 30
    PRO: 
      price: 60
    ADVANCED:
      price: 90
```

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
