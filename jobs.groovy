def project = 'Sergey-Volga/d323dsl'
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branchesList = new groovy.json.JsonSlurper().parse(branchApi.newReader())
def GIT_PATH = "https://github.com/Sergey-Volga/d323dsl.git"

def branches = branchesList.collect{ elem -> /"${elem['name']}"/}
def jobs = (1..4).collect{i -> /MNTLAB-svolga-child${i}-build-job/}

job("MNTLAB-svolga-main-build-job") {
  parameters {
    activeChoiceParam('BRANCH_NAME') {
      choiceType('SINGLE_SELECT')
      groovyScript {
        script(/return ["svolga", "master"]/)
      }
    }
    activeChoiceParam('RUN_JOBS') {
      choiceType('CHECKBOX')
      groovyScript {
        script("return" + jobs.collect{elem -> /"${elem}"/ })
      }
    }
    steps {
      downstreamParameterized {
        trigger('$RUN_JOBS') {
          parameters {
            predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
          }
        }
      }
    }
  }
  scm {
    git {
      remote {
        url(GIT_PATH)
      }
    }
  }
}

for(j in jobs) {
  job(j) {
    parameters {
      activeChoiceParam('BRANCH_NAME') {
        choiceType('SINGLE_SELECT')
        groovyScript {
          script(/return ${branches}/)
        }
      }
    }
    scm {
      git {
        remote {
          url(GIT_PATH)
        }
      }
    }
    steps {
      shell('cat ./script.sh > output.txt')
      shell('tar -cvf $BRANCH_NAME_dsl_script.tar.gz output.txt')
    }
  }
}

