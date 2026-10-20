# Task 13 – Configuration Management Specification (Ansible)

## Server prerequisites for a Yoga portal node

| Category | Item | Value / state | Ansible task (`ansible/site.yml`) |
|---|---|---|---|
| OS | Ubuntu 24.04 LTS | clean image `ubuntu:24.04` | (provisioned by `scripts/provision-node.sh`) |
| Bootstrap | `python3`, `python3-apt` | present | `raw` bootstrap (idempotent check) |
| **Packages** | `openjdk-21-jre-headless` | present | `apt` |
| | `curl`, `procps`, `ca-certificates`, `tzdata` | present | `apt` |
| **Users** | group `yoga` | system group | `group` |
| | user `yoga` | system user, home `/opt/yoga`, shell `/usr/sbin/nologin` | `user` |
| **Folders** | `/opt/yoga` | owner yoga, 0755 | `file` (loop) |
| | `/opt/yoga/releases/<version>` | one folder per release | `file` |
| | `/opt/yoga/config`, `/logs`, `/run`, `/bin` | owner yoga, 0755 | `file` (loop) |
| **Files** | `/opt/yoga/releases/<v>/yoga-portal.war` | release artefact, 0644 | `copy` |
| | `/opt/yoga/config/application.properties` | port, environment, log file, 0640 | `template` → restart |
| | `/opt/yoga/bin/yoga-ctl` | service control script, 0755 | `template` → restart |
| | `/opt/yoga/current` → `releases/<v>` | symlink to the active release | `file state=link` → restart |
| **Ports** | 8080/tcp inside the node | published on the host as **8095** | `wait_for` |
| **Services** | `yoga-portal` (java -jar as user `yoga`) | running | `command yoga-ctl start` (changed only if it had to start) |
| **Health** | `GET /actuator/health` | `{"status":"UP"}` | `uri` with retries |

All values are variables in [`ansible/group_vars/yoga_nodes.yml`](../ansible/group_vars/yoga_nodes.yml).

## Files

```
ansible/
├── ansible.cfg                     # inventory path, python interpreter
├── inventory.ini                   # [yoga_nodes] yoga-node via community.docker connection
├── group_vars/yoga_nodes.yml       # the specification above, as variables
├── site.yml                        # provision + deploy (idempotent)
├── rollback.yml                    # switch to an existing release + verify
└── templates/
    ├── application.properties.j2
    └── yoga-ctl.j2
```

## Design notes

* **Agentless.** The node is a container, so Ansible connects through Docker's exec API
  (`community.docker.docker` connection) and no SSH server is needed. To manage a real VM, change only the inventory line.
* **Idempotent by design.** Every task is declarative (`apt`, `user`, `file`, `copy`, `template`). The service task reports
  *changed* only when it actually starts the process. Handlers restart the app only when config, script or release changed.
* **Releases kept side by side.** `/opt/yoga/releases/1.0.0` and `/opt/yoga/releases/1.1.0` both stay on disk. Switching the `current`
  symlink makes rollback a seconds-long operation.

## First execution

```bash
cd ansible
ansible-playbook site.yml -e release_version=1.0.0 -e war_source=/abs/path/yoga-portal-1.0.0.war
```

The full first-run log is produced by `scripts/provision-node.sh` (`01-first-run.log`).
