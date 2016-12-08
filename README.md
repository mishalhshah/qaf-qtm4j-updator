# qaf-qtm4j-updator

Result updator plugin for <a href="http://www.qmetry.com/qmetry-test-manager-for-jira/" target="_blank">QMetry Test Manager For Jira</a>.

To import results for qas/json and push back results, generate qaf-qtm4j-updator .jar and add it to your lib folder.

Also Add below properties:

integration.param.qtm4j.enabled=true

integration.param.qtm4j.apikey= [Generated API KEY]

integration.param.qtm4j.baseurl=https://qtmcloud.qmetry.com/internal/importResults.do
