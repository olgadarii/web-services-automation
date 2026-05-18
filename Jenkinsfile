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
                    sh """
                        sed -i '' 's|token=.*|token=${GITHUB_TOKEN}|' src/main/resources/config.properties
                        sed -i '' 's|username=.*|username=${GITHUB_USER}|' src/main/resources/config.properties
                        sed -i '' 's|password=.*|password=${GITHUB_PASS}|' src/main/resources/config.properties
                        mvn clean test -Dsurefire.suiteXmlFiles=testng.xml
                    """
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
