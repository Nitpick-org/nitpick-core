# Third-party notices

Nitpick is released under the MIT License (see [LICENSE](LICENSE)). It depends on
or bundles the following third-party components. Their licenses apply to those
components, not to Nitpick's own code.

## JGit 6.10.1 — Eclipse Public License 2.0

- Project: <https://github.com/eclipse-jgit/jgit>
- License: <https://www.eclipse.org/legal/epl-2.0/>
- Source for the exact version: <https://repo1.maven.org/maven2/org/eclipse/jgit/org.eclipse.jgit/6.10.1.202505221210-r/>
- Used by `shared/src/jvmSharedMain` for the desktop and Android git backend.

## Compose Multiplatform 1.12.2 / Material 3 — Apache License 2.0

- Project: <https://www.jetbrains.com/compose-multiplatform/>
- License: <https://www.apache.org/licenses/LICENSE-2.0>
- Used for the shared UI in `shared/src/commonMain`.

## Koog 1.3.0 — Apache License 2.0

- Project: <https://github.com/JetBrains/koog>
- License: <https://www.apache.org/licenses/LICENSE-2.0>
- Used for the review agent in `shared/src/commonMain/kotlin/dev/nitpick/app/review`.

## Ktor 3.3.3 — Apache License 2.0

- Project: <https://ktor.io/>
- License: <https://www.apache.org/licenses/LICENSE-2.0>
- Used by `server/` and by the review-sharing client.

## kotlinx.coroutines / kotlinx.serialization — Apache License 2.0

- Projects: <https://github.com/Kotlin/kotlinx.coroutines>, <https://github.com/Kotlin/kotlinx.serialization>
- License: <https://www.apache.org/licenses/LICENSE-2.0>

## JetBrains Mono — SIL Open Font License 1.1

- Project: <https://www.jetbrains.com/lp/mono/>
- License: <https://openfontlicense.org/>
- Bundled at `shared/src/commonMain/composeResources/font/`; see
  [docs/design/FONT-LICENSES.md](docs/design/FONT-LICENSES.md).

---

If you redistribute the built application, keep this file and the license texts
above with it. The JGit EPL-2.0 notice and its source-availability pointer are
required; the Apache-2.0 and OFL-1.1 texts are included by reference.
