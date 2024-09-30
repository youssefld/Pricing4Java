def calculateStorageOverageFee(extraGBConsumed, fee):
    return extraGBConsumed * fee


def calculateAccountGB(gbPerSeat, seats):
    return gbPerSeat * seats


def account_available_GB(userContext, plan):
    accountGBAvailable = calculateAccountGB(
        plan[userContext["subscription"]]["storage"], userContext["seats"]
    )
    if userContext["cloudStoragePlan"] != None:
        accountGBAvailable += cloudStorage[pedro["cloudStoragePlan"]]["storage"]
    return accountGBAvailable


if __name__ == "__main__":
    pedro = {
        "subscription": "PRO",
        "cloudStoragePlan": "$10",
        "seats": 3,
        "consumedGB": 60,
    }

    plan = {
        "PRO": {"monthlyPrice": 15.99, "annualPrice": 13.32, "storage": 5},
        "BUSINESS-PLUS": {"monthlyPrice": 21.99, "annualPrice": 18.32, "storage": 10},
    }

    cloudStorage = {
        "$10": {"price": 10, "storage": 30, "fee": 1.5},
        "$40": {"price": 40, "storage": 200, "fee": 1.5},
        "$100": {"price": 100, "storage": 1024, "fee": 0.5},
        "$500": {"price": 500, "storage": 5 * 1024, "fee": 0.1},
    }

    accountGBAvailable = account_available_GB(pedro, plan)

    print(f"Available GB in your account: {accountGBAvailable}")

    extraGBConsumed = (
        pedro["consumedGB"] - accountGBAvailable
        if pedro["consumedGB"] > accountGBAvailable
        else 0
    )

    extraPrice = calculateStorageOverageFee(
        extraGBConsumed,
        cloudStorage[pedro["cloudStoragePlan"]]["fee"],
    )
    print(
        f'Cloud Storage AddOn price {cloudStorage[pedro["cloudStoragePlan"]]["price"]}, Overage Fees {extraPrice}'
    )
