#!/usr/bin/env groovy

/* `buildPlugin` step provided by: https://github.com/jenkins-infra/pipeline-library */
buildPlugin(failFast: false,
            configurations: [
                [platform: 'linux',   jdk: '17', jenkins: '2.371'  ],
                [platform: 'linux',   jdk: '11', jenkins: '2.361.1'],
                [platform: 'windows', jdk: '8'                     ],
            ])
