package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class YamlParsingTests {
    @Test
    @Order(10)
    void parsePostmanYamlToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/postman.yml");

        assertEquals("Postman", pricingManager.getSaasName(), "The saasName should be Postman");
        assertTrue(pricingManager.getPlans().get("BASIC") instanceof Plan, "Should be an instance of PricingManager");
        assertEquals(true, pricingManager.getPlans().get("BASIC").getFeatures().get("apiClient").getDefaultValue(),
                "The deafult value of the apiClient feature should be true");
        assertEquals(10000,
                pricingManager.getPlans().get("BASIC").getUsageLimits().get("callsToPostmanApi").getDefaultValue(),
                "The default value of the callsToPostmanApi usageLimit should be 10000, as it must be copied from the defaultValue");
    }

    @Test
    @Order(20)
    void parseFigmaToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/figma.yml");

        assertEquals("Figma", pricingManager.getSaasName(), "The saasName should be Figma");
        assertTrue(pricingManager.getPlans().get("STARTER") instanceof Plan, "Should be an instance of Plan");
        assertEquals(true,
                pricingManager.getPlans().get("STARTER").getFeatures().get("versionHistory").getDefaultValue(),
                "The deafult value of the versionHistory feature should be true");
        assertEquals(100, pricingManager.getPlans().get("STARTER").getUsageLimits().get("teamsLimit").getDefaultValue(),
                "The default value of the teamsLimit usageLimit should be 100, as it must be copied from the defaultValue");
    }

    @Test
    @Order(30)
    void parseMicrosoftBusinessToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/microsoftEnterprise.yml");

        assertEquals("Microsoft 365 Enterpise", pricingManager.getSaasName(),
                "The saasName should be Microsoft 365 Enterpise");
        assertTrue(pricingManager.getPlans().get("APPS_FOR_BUSINESS") instanceof Plan, "Should be an instance of Plan");
        assertEquals(false, pricingManager.getPlans().get("APPS_FOR_BUSINESS").getFeatures().get("sharedCalendars")
                .getDefaultValue(), "The deafult value of the sharedCalendars feature should be false");
        assertEquals(300,
                pricingManager.getPlans().get("BUSINESS_PREMIUM").getUsageLimits().get("webinarsMaxLimit")
                        .getDefaultValue(),
                "The default value of the teamsLimit usageLimit for the plan BUSINESS_PREMIUM should be 300, as it must be copied from the defaultValue");
    }

    @Test
    @Order(40)
    void parseRapidAPIToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/rapidAPI.yml");

        assertEquals("RapidAPI", pricingManager.getSaasName(), "The saasName should be RapidAPI");
        assertTrue(pricingManager.getPlans().get("BASIC") instanceof Plan, "Should be an instance of Plan");
        assertEquals(true, pricingManager.getPlans().get("BASIC").getFeatures().get("search").getDefaultValue(),
                "The deafult value of the search feature should be true");
        assertEquals(Double.POSITIVE_INFINITY,
                pricingManager.getPlans().get("ENTERPRISE").getUsageLimits().get("numberOfBuilders").getValue(),
                "The value of the numberOfBuilders usageLimit for the plan ENTERPRISE should be infinite, is it's customizable");
    }

    @Test
    @Order(50)
    void parseSalesCloudToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/salescloud.yml");

        assertEquals("salescloud", pricingManager.getSaasName(), "The saasName should be salescloud");
        assertTrue(pricingManager.getPlans().get("STARTER") instanceof Plan, "Should be an instance of Plan");
        assertEquals(true,
                pricingManager.getPlans().get("STARTER").getFeatures().get("leadManagement").getDefaultValue(),
                "The deafult value of the leadManagement feature should be true");
        assertEquals(Double.POSITIVE_INFINITY,
                pricingManager.getPlans().get("ENTERPRISE").getUsageLimits().get("numberOfProcessesAndFlows")
                        .getValue(),
                "The value of the numberOfProcessesAndFlows usageLimit for the plan ENTERPRISE should be infinite, is it's customizable");
    }

    @Test
    @Order(60)
    void parseCanvaToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/canva.yml");

        assertEquals("canva", pricingManager.getSaasName(), "The saasName should be canva");
    }

    @Test
    @Order(70)
    void parseClockifyToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/clockify.yml");

        assertEquals("clockify", pricingManager.getSaasName(), "The saasName should be clockify");
        assertEquals(null, pricingManager.getUsageLimits(), "Clockify does not have usageLimits");
        assertEquals(null, pricingManager.getPlans().get("FREE").getUsageLimits(),
                "Clockify does not have usageLimits");
    }

    @Test
    @Order(80)
    void parseGitHubToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/github.yml");

        assertEquals("github", pricingManager.getSaasName(), "The saasName should be github");
    }

    @Test
    @Order(90)
    void parseJiraToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/jira.yml");

        assertEquals("Jira", pricingManager.getSaasName(), "The saasName should be Jira");
    }

    @Test
    @Order(100)
    void parseMondayToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/monday.yml");

        assertEquals("Monday", pricingManager.getSaasName(), "The saasName should be Monday");
    }

    @Test
    @Order(110)
    void parseNavetorToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/navetor.yml");

        assertEquals("Navetor", pricingManager.getSaasName(), "The saasName should be Navetor");
    }

    @Test
    @Order(120)
    void parseOverleaf2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/overleaf.yml");

        assertEquals("Overleaf - Individual", pricingManager.getSaasName(), "The saasName should be Overleaf");
    }

    @Test
    @Order(130)
    void parsePipedriveToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/pipedrive.yml");

        assertEquals("Pipedrive", pricingManager.getSaasName(), "The saasName should be Pipedrive");
    }

    @Test
    @Order(140)
    void parseRipplingToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/rippling.yml");

        assertEquals("Rippling", pricingManager.getSaasName(), "The saasName should be Rippling");
    }

    @Test
    @Order(150)
    void parseWrikeToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/wrike.yml");

        assertEquals("Wrike", pricingManager.getSaasName(), "The saasName should be Wrike");
    }

    @Test
    @Order(160)
    void parseQuip2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/quip.yml");

        assertEquals("Quip", pricingManager.getSaasName(), "The saasName should be Quip");
    }

    @Test
    @Order(170)
    void parseEvernote2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/evernote.yml");

        assertEquals("Evernote", pricingManager.getSaasName(), "The saasName should be Evernote");
    }

    @Test
    @Order(180)
    void parsePlanable2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/planable.yml");

        assertEquals("Planable", pricingManager.getSaasName(), "The saasName should be Planable");
    }

    @Test
    @Order(190)
    void parseDatabox2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/databox.yml");

        assertEquals("Databox", pricingManager.getSaasName(), "The saasName should be Databox");
    }

    @Test
    @Order(200)
    void parseTableau2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/tableau.yml");

        assertEquals("Tableau", pricingManager.getSaasName(), "The saasName should be Tableau");
    }

    @Test
    @Order(210)
    void parseBuffer2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/buffer.yml");

        assertEquals("Buffer", pricingManager.getSaasName(), "The saasName should be Buffer");
    }

    @Test
    @Order(210)
    void parseClickUp2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/clickup.yml");

        assertEquals("ClickUp", pricingManager.getSaasName(), "The saasName should be ClickUp");
    }

    @Test
    @Order(220)
    void parseUserGuiding2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/userguiding.yml");

        assertEquals("UserGuiding", pricingManager.getSaasName(), "The saasName should be UserGuiding");
    }

    @Test
    @Order(230)
    void parseHyperContext2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/hypercontext.yml");

        assertEquals("HyperContext", pricingManager.getSaasName(), "The saasName should be HyperContext");
    }

    @Test
    @Order(240)
    void parseTableau2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/tableau.yml");

        assertEquals("Tableau", pricingManager.getSaasName(), "The saasName should be Tableau");
    }

    @Test
    @Order(250)
    void parseTableau2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/tableau.yml");

        assertEquals("Tableau", pricingManager.getSaasName(), "The saasName should be Tableau");
    }

    @Test
    @Order(260)
    void parseTableau2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/tableau.yml");

        assertEquals("Tableau", pricingManager.getSaasName(), "The saasName should be Tableau");
    }

    @Test
    @Order(270)
    void parseTableau2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/tableau.yml");

        assertEquals("Tableau", pricingManager.getSaasName(), "The saasName should be Tableau");
    }

    @Test
    @Order(270)
    void parseTableau2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/tableau.yml");

        assertEquals("Tableau", pricingManager.getSaasName(), "The saasName should be Tableau");
    }

    @Test
    @Order(280)
    void parseQuip2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/quip.yml");

        assertEquals("Quip", pricingManager.getSaasName(), "The saasName should be Quip");
    }

    @Test
    @Order(290)
    void parseQuip2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/quip.yml");

        assertEquals("Quip", pricingManager.getSaasName(), "The saasName should be Quip");
    }

    @Test
    @Order(300)
    void parseQuip2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/quip.yml");

        assertEquals("Quip", pricingManager.getSaasName(), "The saasName should be Quip");
    }

    @Test
    @Order(310)
    void parseQuip2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/quip.yml");

        assertEquals("Quip", pricingManager.getSaasName(), "The saasName should be Quip");
    }

    @Test
    @Order(320)
    void parseQuip2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/quip.yml");

        assertEquals("Quip", pricingManager.getSaasName(), "The saasName should be Quip");
    }

    @Test
    @Order(330)
    void parseUserGuiding2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/userguiding.yml");

        assertEquals("UserGuiding", pricingManager.getSaasName(), "The saasName should be UserGuiding");
    }

    @Test
    @Order(340)
    void parseUserGuiding2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/userguiding.yml");

        assertEquals("UserGuiding", pricingManager.getSaasName(), "The saasName should be UserGuiding");
    }

    @Test
    @Order(350)
    void parseUserGuiding2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/userguiding.yml");

        assertEquals("UserGuiding", pricingManager.getSaasName(), "The saasName should be UserGuiding");
    }

    @Test
    @Order(360)
    void parseUserGuiding2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/userguiding.yml");

        assertEquals("UserGuiding", pricingManager.getSaasName(), "The saasName should be UserGuiding");
    }

    @Test
    @Order(370)
    void parseDatabox2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/databox.yml");

        assertEquals("Databox", pricingManager.getSaasName(), "The saasName should be Databox");
    }

    @Test
    @Order(380)
    void parseDatabox2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/databox.yml");

        assertEquals("Databox", pricingManager.getSaasName(), "The saasName should be Databox");
    }

    @Test
    @Order(390)
    void parseDatabox2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/databox.yml");

        assertEquals("Databox", pricingManager.getSaasName(), "The saasName should be Databox");
    }

    @Test
    @Order(400)
    void parseDatabox2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/databox.yml");

        assertEquals("Databox", pricingManager.getSaasName(), "The saasName should be Databox");
    }

    @Test
    @Order(410)
    void parseDatabox2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/databox.yml");

        assertEquals("Databox", pricingManager.getSaasName(), "The saasName should be Databox");
    }

    @Test
    @Order(420)
    void parseEvernote2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/evernote.yml");

        assertEquals("Evernote", pricingManager.getSaasName(), "The saasName should be Evernote");
    }

    @Test
    @Order(430)
    void parseEvernote2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/evernote.yml");

        assertEquals("Evernote", pricingManager.getSaasName(), "The saasName should be Evernote");
    }

    @Test
    @Order(440)
    void parseEvernote2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/evernote.yml");
        
        assertEquals("Evernote", pricingManager.getSaasName(), "The saasName should be Evernote");
    }

    @Test
    @Order(450)
    void parseEvernote2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/evernote.yml");
        
        assertEquals("Evernote", pricingManager.getSaasName(), "The saasName should be Evernote");
    }

    @Test
    @Order(460)
    void parseEvernote2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/evernote.yml");
        
        assertEquals("Evernote", pricingManager.getSaasName(), "The saasName should be Evernote");
    }

    @Test
    @Order(470)
    void parseCanva2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/canva.yml");
        
        assertEquals("Canva", pricingManager.getSaasName(), "The saasName should be Canva");
    }

    @Test
    @Order(480)
    void parseCanva2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/canva.yml");
        
        assertEquals("Canva", pricingManager.getSaasName(), "The saasName should be Canva");
    }

    @Test
    @Order(480)
    void parseCanva2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/canva.yml");
        
        assertEquals("Canva", pricingManager.getSaasName(), "The saasName should be Canva");
    }

    @Test
    @Order(490)
    void parseCanva2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/canva.yml");
        
        assertEquals("Canva", pricingManager.getSaasName(), "The saasName should be Canva");
    }

    @Test
    @Order(500)
    void parseCanva2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/canva.yml");
        
        assertEquals("Canva", pricingManager.getSaasName(), "The saasName should be Canva");
    }

    @Test
    @Order(510)
    void parseClockify2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/clockify.yml");
        
        assertEquals("Clockify", pricingManager.getSaasName(), "The saasName should be Clockify");
    }

    @Test
    @Order(520)
    void parseClockify2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/clockify.yml");
        
        assertEquals("Clockify", pricingManager.getSaasName(), "The saasName should be Clockify");
    }
    
    @Test
    @Order(530)
    void parseGithub2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/github.yml");
        
        assertEquals("Github", pricingManager.getSaasName(), "The saasName should be Github");
    }
    
    @Test
    @Order(540)
    void parseGithub2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/github.yml");
        
        assertEquals("Github", pricingManager.getSaasName(), "The saasName should be Github");
    }

    @Test
    @Order(550)
    void parseGithub2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/github.yml");
        
        assertEquals("Github", pricingManager.getSaasName(), "The saasName should be Github");
    }

    @Test
    @Order(560)
    void parseGithub2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/github.yml");
        
        assertEquals("Github", pricingManager.getSaasName(), "The saasName should be Github");
    }

    @Test
    @Order(570)
    void parseGithub2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/github.yml");
        
        assertEquals("Github", pricingManager.getSaasName(), "The saasName should be Github");
    }

    @Test
    @Order(580)
    void parseFigma2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/figma.yml");
        
        assertEquals("Figma", pricingManager.getSaasName(), "The saasName should be Figma");
    }

    @Test
    @Order(590)
    void parseFigma2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/figma.yml");
        
        assertEquals("Figma", pricingManager.getSaasName(), "The saasName should be Figma");
    }

    @Test
    @Order(600)
    void parseFigma2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/figma.yml");
        
        assertEquals("Figma", pricingManager.getSaasName(), "The saasName should be Figma");
    }

    @Test
    @Order(610)
    void parseFigma2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/figma.yml");
        
        assertEquals("Figma", pricingManager.getSaasName(), "The saasName should be Figma");
    }

    @Test
    @Order(620)
    void parseFigma2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/figma.yml");
        
        assertEquals("Figma", pricingManager.getSaasName(), "The saasName should be Figma");
    }

    @Test
    @Order(630)
    void parseJira2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/jira.yml");
        
        assertEquals("Jira", pricingManager.getSaasName(), "The saasName should be Jira");
    }

    @Test
    @Order(640)
    void parseJira2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/jira.yml");
        
        assertEquals("Jira", pricingManager.getSaasName(), "The saasName should be Jira");
    }

    @Test
    @Order(650)
    void parseJira2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/jira.yml");
        
        assertEquals("Jira", pricingManager.getSaasName(), "The saasName should be Jira");
    }

    @Test
    @Order(660)
    void parseJira2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/jira.yml");
        
        assertEquals("Jira", pricingManager.getSaasName(), "The saasName should be Jira");
    }

    @Test
    @Order(670)
    void parseJira2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/jira.yml");
        
        assertEquals("Jira", pricingManager.getSaasName(), "The saasName should be Jira");
    }

    @Test
    @Order(680)
    void parsePostman2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/postman.yml");
        
        assertEquals("Postman", pricingManager.getSaasName(), "The saasName should be Postman");
    }

    @Test
    @Order(690)
    void parsePostman2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/postman.yml");
        
        assertEquals("Postman", pricingManager.getSaasName(), "The saasName should be Postman");
    }

    @Test
    @Order(700)
    void parsePostman2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/postman.yml");
        
        assertEquals("Postman", pricingManager.getSaasName(), "The saasName should be Postman");
    }

    @Test
    @Order(710)
    void parsePostman2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/postman.yml");
        
        assertEquals("Postman", pricingManager.getSaasName(), "The saasName should be Postman");
    }

    @Test
    @Order(720)
    void parseZapier2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/zapier.yml");
        
        assertEquals("Zapier", pricingManager.getSaasName(), "The saasName should be Zapier");
    }

    @Test
    @Order(730)
    void parseZapier2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/zapier.yml");
        
        assertEquals("Zapier", pricingManager.getSaasName(), "The saasName should be Zapier");
    }

    @Test
    @Order(740)
    void parseZapier2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/zapier.yml");
        
        assertEquals("Zapier", pricingManager.getSaasName(), "The saasName should be Zapier");
    }

    @Test
    @Order(740)
    void parseZapier2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/zapier.yml");
        
        assertEquals("Zapier", pricingManager.getSaasName(), "The saasName should be Zapier");
    }

    @Test
    @Order(750)
    void parseZapier2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/zapier.yml");
        
        assertEquals("Zapier", pricingManager.getSaasName(), "The saasName should be Zapier");
    }

    @Test
    @Order(760)
    void parseTrustmary2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/trustmary.yml");
        
        assertEquals("Trustmary", pricingManager.getSaasName(), "The saasName should be Trustmary");
    }

    @Test
    @Order(770)
    void parseTrustmary2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/trustmary.yml");
        
        assertEquals("Trustmary - Full Suite", pricingManager.getSaasName(), "The saasName should be Trustmary");
    }

    @Test
    @Order(780)
    void parseTrustmary2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/trustmary.yml");
        
        assertEquals("Trustmary", pricingManager.getSaasName(), "The saasName should be Trustmary");
    }

    @Test
    @Order(790)
    void parseTrustmary2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/trustmary.yml");
        
        assertEquals("Trustmary - Collect", pricingManager.getSaasName(), "The saasName should be Trustmary");
    }

    @Test
    @Order(800)
    void parseTrustmary2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/trustmary.yml");
        
        assertEquals("Trustmary - Collect", pricingManager.getSaasName(), "The saasName should be Trustmary");
    }

    @Test
    @Order(810)
    void parseOverleaf2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/overleaf.yml");
        
        assertEquals("Overleaf", pricingManager.getSaasName(), "The saasName should be Overleaf");
    }

    @Test
    @Order(820)
    void parseOverleaf2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/overleaf.yml");
        
        assertEquals("Overleaf", pricingManager.getSaasName(), "The saasName should be Overleaf");
    }

    @Test
    @Order(830)
    void parseOverleaf2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/overleaf.yml");
        
        assertEquals("Overleaf", pricingManager.getSaasName(), "The saasName should be Overleaf");
    }

    @Test
    @Order(840)
    void parseOverleaf2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/overleaf.yml");
        
        assertEquals("Overleaf", pricingManager.getSaasName(), "The saasName should be Overleaf");
    }

    @Test
    @Order(850)
    void parseOverleaf2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/overleaf.yml");
        
        assertEquals("Overleaf - Individual", pricingManager.getSaasName(), "The saasName should be Overleaf");
    }

    @Test
    @Order(850)
    void parseCrowdcast2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/crowdcast.yml");
        
        assertEquals("Crowdcast", pricingManager.getSaasName(), "The saasName should be Crowdcast");
    }

    @Test
    @Order(860)
    void parseCrowdcast2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/crowdcast.yml");
        
        assertEquals("Crowdcast", pricingManager.getSaasName(), "The saasName should be Crowdcast");
    }

    @Test
    @Order(870)
    void parseCrowdcast2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/crowdcast.yml");
        
        assertEquals("Crowdcast", pricingManager.getSaasName(), "The saasName should be Crowdcast");
    }

    @Test
    @Order(880)
    void parseCrowdcast2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/crowdcast.yml");
        
        assertEquals("Crowdcast", pricingManager.getSaasName(), "The saasName should be Crowdcast");
    }

    @Test
    @Order(890)
    void parseCrowdcast2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/crowdcast.yml");
        
        assertEquals("Crowdcast", pricingManager.getSaasName(), "The saasName should be Crowdcast");
    }

    @Test
    @Order(900)
    void parseMailchimp2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/mailchimp.yml");
        
        assertEquals("MailChimp", pricingManager.getSaasName(), "The saasName should be MailChimp");
    }

    @Test
    @Order(910)
    void parseMailchimp2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/mailchimp.yml");
        
        assertEquals("MailChimp", pricingManager.getSaasName(), "The saasName should be MailChimp");
    }

    @Test
    @Order(910)
    void parseMailchimp2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/mailchimp.yml");
        
        assertEquals("MailChimp - Marketing", pricingManager.getSaasName(), "The saasName should be MailChimp");
    }

    @Test
    @Order(920)
    void parseMailchimp2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/mailchimp.yml");
        
        assertEquals("MailChimp - Marketing", pricingManager.getSaasName(), "The saasName should be MailChimp");
    }

    @Test
    @Order(930)
    void parseMailchimp2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/mailchimp.yml");
        
        assertEquals("MailChimp - Marketing", pricingManager.getSaasName(), "The saasName should be MailChimp");
    }

    @Test
    @Order(940)
    void parseMailchimp2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/mailchimp.yml");
        
        assertEquals("MailChimp - Marketing", pricingManager.getSaasName(), "The saasName should be MailChimp");
    }

    @Test
    @Order(950)
    void parseDeskera2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/deskera.yml");
        
        assertEquals("Deskera", pricingManager.getSaasName(), "The saasName should be Deskera");
    }

    @Test
    @Order(960)
    void parseDeskera2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/deskera.yml");
        
        assertEquals("Deskera", pricingManager.getSaasName(), "The saasName should be Deskera");
    }

    @Test
    @Order(970)
    void parseDeskera2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/deskera.yml");
        
        assertEquals("Deskera", pricingManager.getSaasName(), "The saasName should be Deskera");
    }

    @Test
    @Order(960)
    void parseDeskera2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/deskera.yml");
        
        assertEquals("Deskera", pricingManager.getSaasName(), "The saasName should be Deskera");
    }

    @Test
    @Order(970)
    void parseSalesforce2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/salesforce.yml");
        
        assertEquals("Salesforce - SalesCloud", pricingManager.getSaasName(), "The saasName should be Salesforce");
    }

    @Test
    @Order(970)
    void parseSalesforce2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/salesforce.yml");
        
        assertEquals("Salesforce - SalesCloud", pricingManager.getSaasName(), "The saasName should be Salesforce");
    }

    @Test
    @Order(980)
    void parseSalesforce2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/salesforce.yml");
        
        assertEquals("Salesforce - SalesCloud", pricingManager.getSaasName(), "The saasName should be Salesforce");
    }

    @Test
    @Order(990)
    void parseSalesforce2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/salesforce.yml");
        
        assertEquals("Salesforce - SalesCloud", pricingManager.getSaasName(), "The saasName should be Salesforce");
    }

    @Test
    @Order(1000)
    void parseHyperContext2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/hypercontext.yml");
        
        assertEquals("HyperContext", pricingManager.getSaasName(), "The saasName should be HyperContext");
    }

    @Test
    @Order(1110)
    void parseHyperContext2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/hypercontext.yml");
        
        assertEquals("HyperContext", pricingManager.getSaasName(), "The saasName should be HyperContext");
    }

    @Test
    @Order(1120)
    void parseHyperContext2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/hypercontext.yml");
        
        assertEquals("HyperContext", pricingManager.getSaasName(), "The saasName should be HyperContext");
    }

    @Test
    @Order(1130)
    void parseDropbox2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/dropbox.yml");
        
        assertEquals("Dropbox", pricingManager.getSaasName(), "The saasName should be Dropbox");
    }

    @Test
    @Order(1140)
    void parseDropbox2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/dropbox.yml");
        
        assertEquals("Dropbox", pricingManager.getSaasName(), "The saasName should be Dropbox");
    }

    @Test
    @Order(1150)
    void parseDropbox2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/dropbox.yml");
        
        assertEquals("Dropbox", pricingManager.getSaasName(), "The saasName should be Dropbox");
    }

    @Test
    @Order(1160)
    void parseDropbox2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/dropbox.yml");
        
        assertEquals("Dropbox", pricingManager.getSaasName(), "The saasName should be Dropbox");
    }

    @Test
    @Order(1170)
    void parseBox2019ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2019/box.yml");
        
        assertEquals("Box", pricingManager.getSaasName(), "The saasName should be Box");
    }

    @Test
    @Order(1180)
    void parseBox2020ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2020/box.yml");
        
        assertEquals("Box", pricingManager.getSaasName(), "The saasName should be Box");
    }

    @Test
    @Order(1190)
    void parseBox2021ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2021/box.yml");
        
        assertEquals("Box", pricingManager.getSaasName(), "The saasName should be Box");
    }

    @Test
    @Order(1200)
    void parseBox2022ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2022/box.yml");
        
        assertEquals("Box", pricingManager.getSaasName(), "The saasName should be Box");
    }

    @Test
    @Order(1210)
    void parseBox2023ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2023/box.yml");
        
        assertEquals("Box", pricingManager.getSaasName(), "The saasName should be Box");
    }

    @Test
    @Order(1220)
    void parseBox2024ToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/2024/box.yml");
        
        assertEquals("Box", pricingManager.getSaasName(), "The saasName should be Box");
    }
}
