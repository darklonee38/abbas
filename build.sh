#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
DEST_DIR="$DIR/dist"
mkdir -p "$DEST_DIR"
export NO_GS=true
TASK='apk'
BUILD_TYPE='Release'

while (($#)); do
    case "$1" in
        -c)
            CLEAN='clean'
            ;;
        -b)
            TASK='aab'
            ;;
        -d)
            BUILD_TYPE='Debug'
            ;;
        *)
            echo "Unknown argument: $1" >&2
            exit 1
            ;;
    esac
    shift
done

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$ANDROID_SDK_ROOT" && -f "$DIR/local.properties" ]]; then
    ANDROID_SDK_ROOT="$(sed -n 's/^sdk.dir=//p' "$DIR/local.properties" | tail -n 1)"
fi

if [[ -z "$ANDROID_SDK_ROOT" ]]; then
    echo 'ANDROID_SDK_ROOT (or ANDROID_HOME) is not set and sdk.dir was not found in local.properties' >&2
    exit 1
fi
export ANDROID_SDK_ROOT
echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"

CMAKE_PATH=''
if [[ -d "$ANDROID_SDK_ROOT/cmake" ]]; then
    CMAKE_PATH="$(find "$ANDROID_SDK_ROOT/cmake" -mindepth 2 -maxdepth 2 -type d -name bin -print | sort -V | tail -n 1)"
fi
if [[ -n "$CMAKE_PATH" ]]; then
    echo "CMAKE_PATH=$CMAKE_PATH"
    export PATH="$CMAKE_PATH:$PATH"
else
    echo 'No Android SDK CMake bin directory found; continuing without adding CMake to PATH.'
fi

cd "$DIR"

# ABBAS is one app/package for phone and Android Auto.
# APK builds publish exactly one universal package, never separate Mobile/Auto or ABI-specific products.
build() {
    local ext="$TASK"
    local app_flavor="${APP_ID_SFX:-}"
    local app_sfx="${APP_ID_SFX:-}"
    local properties_file="$DIR/local.properties"

    if [[ -z "$app_flavor" && -f "$properties_file" ]]; then
        app_flavor="$(sed -n "s/^"${TASK}"Flavor=//p" "$properties_file" | tail -n 1)"
    fi
    if [[ -z "$app_sfx" && -f "$properties_file" ]]; then
        app_sfx="$(sed -n "s/^"${TASK}"IdSfx=//p" "$properties_file" | tail -n 1)"
    fi

    local -a gradle_args=()
    if [[ -n "${CLEAN:-}" ]]; then
        gradle_args+=("$CLEAN")
    fi
    gradle_args+=(verifyWebOnlyProductionGraph)

    local task
    local output_root
    if [[ "$TASK" == 'apk' ]]; then
        task="package${app_flavor}Auto${BUILD_TYPE}UniversalApk"
        output_root="fermata/build/outputs/apk_from_bundle"
    else
        task="bundle${app_flavor}Auto${BUILD_TYPE}"
        output_root="fermata/build/outputs/bundle"
    fi
    gradle_args+=("fermata:$task")
    if [[ -n "$app_sfx" ]]; then
        gradle_args+=("-PAPP_ID_SFX=$app_sfx")
    fi

    # Remove stale ABBAS artifacts so an old build cannot be mistaken for the new output.
    if [[ -d "$output_root" ]]; then
        find "$output_root" -type f -name "abbas*.$ext" -delete
    fi

    ./gradlew "${gradle_args[@]}"

    local -a artifacts=()
    if [[ -d "$output_root" ]]; then
        mapfile -t artifacts < <(find "$output_root" -type f -name "abbas*.$ext" -print)
    fi
    if [[ "${#artifacts[@]}" -ne 1 ]]; then
        echo "Expected exactly one ABBAS $ext artifact, found ${#artifacts[@]}" >&2
        exit 1
    fi

    local path="${artifacts[0]}"
    if [[ "$TASK" == 'apk' ]]; then
        if ! command -v jar >/dev/null 2>&1; then
            echo 'The JDK jar tool is required to inspect the universal APK' >&2
            exit 1
        fi
        if jar tf "$path" | grep -Eqi 'jlibtorrent|libtorrent'; then
            echo 'Universal APK contains a forbidden libtorrent artifact' >&2
            exit 1
        fi
    fi
    local version="${path##*abbas-}"
    version="${version%%-*}"
    local dst="$DEST_DIR/ABBAS-${version}.$ext"
    cp "$path" "$dst"
    echo "Built $dst"
}

build
