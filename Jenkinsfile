pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                withCredentials([
                    string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN'),
                    usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_PASS')
                ]) {
                    sh '''
                        python3 -c "
import os
lines = [
    'base_url=https://api.github.com\\n',
    'token=' + os.environ['GITHUB_TOKEN'] + '\\n',
    'username=' + os.environ['GITHUB_USER'] + '\\n',
    'password=' + os.environ['GITHUB_PASS'] + '\\n'
]
open('src/main/resources/config.properties', 'w').writelines(lines)
"
                        mvn clean test -Dsurefire.suiteXmlFiles=testng.xml
                    '''
                }
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
        success {
            echo 'All tests passed!'
        }
        failure {
            echo 'Tests failed — check the TestNG report in target/surefire-reports.'
        }
    }
}
