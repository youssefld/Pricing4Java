package io.github.isagroup.services.updaters;

import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import io.github.isagroup.services.yaml.SkipNullRepresenter;

public class Demo {

    public static void main(String[] args) {

        DumperOptions d = new DumperOptions();
        d.setDefaultFlowStyle(FlowStyle.BLOCK);
        d.setPrettyFlow(true);
        d.setIndent(2);
        Yaml yaml = new Yaml(new SkipNullRepresenter(), d);
        String file10 = """
                version: 1.0
                saasName: petclinic
                day: 15
                month: 1
                year: 2024
                currency: EUR
                hasAnnualPayment: false
                features:
                  maxPets:
                    description: maxPets description
                    valueType: NUMERIC
                    defaultValue: 2
                    expression: userContext['pets'] < planContext['maxPets']
                    serverExpression: userContext['pets'] <= planContext['maxPets']
                    type: DOMAIN
                  maxVisitsPerMonthAndPet:
                    description: maxVisitsPerMonthAndPet description
                    valueType: NUMERIC
                    defaultValue: 1
                    expression: 1==1
                    serverExpression: 1==1
                    type: DOMAIN
                  supportPriority:
                    description: supportPriority description
                    valueType: TEXT
                    defaultValue: LOW
                    expression: ""
                    type: SUPPORT
                  haveCalendar:
                    description: haveCalendar description
                    valueType: BOOLEAN
                    defaultValue: false
                    expression: planContext['haveCalendar']
                    type: DOMAIN
                  havePetsDashboard:
                    description: havePetsDashboard description
                    valueType: BOOLEAN
                    defaultValue: false
                    expression: planContext['havePetsDashboard']
                    type: DOMAIN
                  haveVetSelection:
                    description: haveVetSelection description
                    valueType: BOOLEAN
                    defaultValue: false
                    expression: planContext['haveVetSelection']
                    type: DOMAIN
                  haveOnlineConsultation:
                    description: haveOnlineConsultation description
                    valueType: BOOLEAN
                    defaultValue: false
                    expression: planContext['haveOnlineConsultation']
                    type: DOMAIN
                usageLimits:
                  maxPets:
                    description: ""
                    valueType: NUMERIC
                    defaultValue: 10
                    unit: pet
                    type: NON_RENEWABLE
                    linkedFeatures:
                      - maxPets
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                  ADVANCED:
                    description: Advanced plan
                    monthlyPrice: 5.0
                    annualPrice: 5.0
                    unit: user/month
                    features:
                      maxPets:
                        value: 4
                      maxVisitsPerMonthAndPet:
                        value: 3
                      supportPriority:
                        value: MEDIUM
                      haveCalendar:
                        value: true
                      havePetsDashboard:
                        value: false
                      haveVetSelection:
                        value: true
                      haveOnlineConsultation:
                        value: false
                    usageLimits: null
                  PRO:
                    description: Pro plan
                    monthlyPrice: 10.0
                    annualPrice: 10.0
                    unit: user/month
                    features:
                      maxPets:
                        value: 7
                      maxVisitsPerMonthAndPet:
                        value: 6
                      supportPriority:
                        value: HIGH
                      haveCalendar:
                        value: true
                      havePetsDashboard:
                        value: true
                      haveVetSelection:
                        value: true
                      haveOnlineConsultation:
                        value: true
                    usageLimits: null
                addOns: null
                                """;
        Map<String, Object> configFile = yaml.load(file10);

        try {
            Map<String, Object> result = updater(configFile, Version.V1_2);
            System.out.println(yaml.dump(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> updater(Map<String, Object> configFile, Version version) {

        Updater updater = new YamlUpdater(configFile);

        switch (version) {
            case V1_0 -> updater = new YamlUpdater(configFile);
            case V1_1 -> updater = new V11Updater(configFile);
            case V1_2 -> updater = new V12Updater(configFile);
        }

        try {
            return updater.update();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
