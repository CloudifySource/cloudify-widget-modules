// Generated on 2014-02-02 using generator-angular 0.3.0
'use strict';

module.exports = function (grunt) {
    require('time-grunt')(grunt);
    // load all grunt tasks
    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

    var deployOpts;
    try { // optional file
        deployOpts = grunt.file.readJSON('dev/deploy.json');
    }catch(e){
        deployOpts = { 'privateKey' : 'Gruntfile.js'};
    }

    grunt.initConfig({
        deployOpts: deployOpts,
        pkg: grunt.file.readJSON('package.json'),
        sftp: {
            test: {
                files: {
                    'artifacts' : 'artifacts/**'
                },
                options: {
                    'username' : '<%=deployOpts.username%>',
                    'privateKey' : grunt.file.read(deployOpts.privateKey),
                    'host' : '<%=deployOpts.host%>',
                    'path' : '<%=deployOpts.path%>/<%=pkg.name%>/<%=pkg.version%>',
                    'createDirectories' : true,
                    'showProgress': true,
                    'srcBasePath' : 'artifacts'
                }
            }
        },
        clean: {
            artifacts: {
                files: [
                    {
                        dot:true,
                        src: [
                            'artifacts',
                            'tempDir'
                        ]
                    }
                ]
            }

        },
        // Put files not handled in other tasks here
        copy: {
            artifacts : {
                files: [
                    {
                        dest: 'artifacts/',
                        src: [
                            'build.id'
                        ]
                    },
                    {
                        'expand':true,
                        'dest' : 'artifacts/',
                        'cwd' : 'cloudify-widget-pool-manager-website/target/',
                        'src' : 'website-1.0.0.jar'

                    },
                    {
                        'expand':true,
                        'dest': 'artifacts/',
                        'cwd' : 'cloudify-widget-pool-manager-website/build',
                        'src' : ['install.sh']
                    }
                ]
            },
            preCompress:{
                files: [
                    {
                        'expand' : true,
                        'cwd' : 'cloudify-widget-pool-manager-website/src/main/resources/schema/',
                        'dest' : 'tempDir/manager-schema/',
                        'src': [
                            '*.sql'
                        ]
                    },
                    {
                        'expand':true,
                        'cwd' : 'cloudify-widget-pool-manager/src/main/resources/sql/',
                        'dest' : 'tempDir/website-schema/',
                        'src': [
                            '*.sql'
                        ]
                    },
                    {
                        'expand':true,
                        'cwd' : 'cloudify-widget-pool-manager-website/',
                        'dest' :'tempDir',
                        'src':[
                            'build/**/*'
                        ]
                    },
                    {
                        'expand':true,
                        'cwd' : 'cloudify-widget-pool-manager-website/target',
                        'dest': 'tempDir',
                        'src': ['website-1.0.0.jar']
                    },
                    {
                        'expand':true,
                        'cwd' : 'cloudify-widget-pool-manager-website/target/dependency',
                        'dest': 'tempDir/lib',
                        'src' : ['**/*']
                    },
                    {

                        'expand':true,
                        'dot': true,
                        'dest': 'tempDir',
                        'src' : ['build.id']

                    }
                ]
            }
        },
        'compress': {
            main : {
                options: {
                    mode:'tar',
                    archive: 'artifacts/<%=pkg.name%>-<%=pkg.version%>.tar',
                    pretty:true
                },
                files:[
                    {
                        expand:true,
                        pretty:true,
                        cwd:'tempDir',
//                        dest: 'guy',
                        'src': ['**/*']
                    }
                ]


            }
        },
        'run':{
            'mvnCleanInstall' : {
                cmd: 'mvn',
                args: [
                    'clean',
                    'install'
                ]
            }
        }
    });

    grunt.registerTask('writeBuildId',
        function(){
            grunt.file.write('build.id', require('os').hostname()  + '-' + new Date().getTime());
        }
    );

    grunt.registerTask('deploy', [
        'clean',
        'run:mvnCleanInstall',
        'writeBuildId',
        'copy:preCompress',
        'compress',
        'copy:artifacts',
        'sftp'
    ]);
};