

pipeline {
    agent any
      environment {
        PIPELINE_BUILD_IMAGE = "api-pollux-userauth"
        PIPELINE_NAME_SPACE = "pollux"
        PIPELINE_REPLICA = 2
        PIPELINE_LOAD_BALANCER_PORT=9092
	      PIPELINE_EUREKA_SERVICE_ADDRESS = "http://eureka-0.eureka.cosmos.svc.cluster.local:8080/eureka,http://eureka-1.eureka.cosmos.svc.cluster.local:8080/eureka,http://eureka-2.eureka.cosmos.svc.cluster.local:8080/eureka,http://eureka-3.eureka.cosmos.svc.cluster.local:8080/eureka,http://eureka-4.eureka.cosmos.svc.cluster.local:8080/eureka,http://eureka-5.eureka.cosmos.svc.cluster.local:8080/eureka"
	      PIPELINE_IMAGE = "172.16.0.72:8443/${env.PIPELINE_NAME_SPACE}/${env.PIPELINE_BUILD_IMAGE}:${env.BUILD_NUMBER}"
        def server = ''
        def name_space = "${env.PIPELINE_NAME_SPACE}"
        def build_image = "${env.PIPELINE_BUILD_IMAGE}"
      }

    stages {
        stage('Checkout') {
            steps {
               
                 git branch:'biznetgio1', credentialsId: 'biznetgio1', url: 'http://128.21.24.60:8888/pollux/api-pollux-userauth'
               
            }
        }
        
      stage('build') {
            steps {
              sh "mvn clean package"
           }
      }
           
     stage('dockerized') {
            steps{
                echo "Build Docker images ${env.PIPELINE_BUILD_IMAGE}"
                sh "docker build -t ${env.PIPELINE_BUILD_IMAGE} ."
              
            }
 
      }
    stage('Deploy To Docker Registry') {
            steps {
                echo "Docker Image Tag Name: ${env.PIPELINE_BUILD_IMAGE}"
                sh "docker tag ${env.PIPELINE_BUILD_IMAGE} ${env.PIPELINE_IMAGE}"
                sh "docker push ${env.PIPELINE_IMAGE}"
           }
           

    }
    
    stage("replace Env"){
        steps{
        	sh "printenv"
            echo "replace env"
            sh "ls k8s/"
            sh "envsubst < k8s/deployment.yaml > k8s/deployment${PIPELINE_NAME_SPACE}${PIPELINE_BUILD_IMAGE}.yaml "
            sh "cat k8s/deployment${PIPELINE_NAME_SPACE}${PIPELINE_BUILD_IMAGE}.yaml"            
            sh "pwd"
              
        }
    }
    
        stage('deploy to kubernetes') {
            steps {
                sh "ls"
                sh '''
                ssh administrator@jenkins whoami
                scp k8s/deployment${name_space}${build_image}.yaml administrator@jenkins:~/.
                ssh administrator@jenkins kubectl apply -f deployment${name_space}${build_image}.yaml
                ssh administrator@jenkins kubectl -n ${name_space} rollout status deployment.app/${build_image}
                '''
                sh "docker rmi -f ${env.PIPELINE_IMAGE}"
           }
    }

    
}
post {
      always {
        discordSend description: 'Jenkins Pipeline Build', enableArtifactsList: true, footer: "Build ${JOB_NAME} #${BUILD_NUMBER}", image: '', link: env.BUILD_URL, result: currentBuild.currentResult, scmWebUrl: '', showChangeset: true, thumbnail: '', title: "${JOB_NAME} #${BUILD_NUMBER}", webhookURL: 'https://discord.com/api/webhooks/1167421282934079548/d2VMeAsU9Z1Qfm3aXvAKhOtisUh7_n3GRwB0hzXu33eUCFAsDobAaEQxMRgnXsbMREqT'
      }
    }
}
