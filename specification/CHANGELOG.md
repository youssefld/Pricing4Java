# Yaml4SaaS Specification Changelog

:warning: If no `version` is specified version 1.0 is set by default

## Version 1.0:


## Version 1.1:

### :white_check_mark: New:
- new field `createdAt` to replace `day` `month` and `year`, see this [section](#deleted)
- new field `starts` to indicate the beginning of operation of the pricing
- new field `ends` to indicate the ending of operation of the pricing
- new field createdAt that replaces old syntax of version 1.0 day,month,year
- now addOns can depend on addOns putting the name of the addOn in the `avaliableFor` field 


### :x: Deleted:

- `day` `month` and `year` were used to indicate the day when the pricing was modeled.
These fields has been deleted as in yaml it is possible to specify a date.
Use new field `createdAt` instead.

### :up: Improvements:

- Now you can skip duplicating the same `expression` in the `serverExpression` in a feature.
`serverExpression` has the same evaluation in `expression` by default. You can still
overwrite `serverExpression` if you want to be different.
