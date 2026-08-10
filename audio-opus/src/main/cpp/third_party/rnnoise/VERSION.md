# RNNoise provenance

- Upstream: https://github.com/xiph/rnnoise
- Release: v0.2
- Source commit: 904a876dce1f9ab8860c0a5000ed151f9f6eef58
- Model version: 0b50c45 (full model)
- Model archive: https://media.xiph.org/rnnoise/models/rnnoise_data-0b50c45.tar.gz
- Model archive SHA-256: 4AC81C5C0884EC4BD5907026AAAE16209B7B76CD9D7F71AF582094A2F98F4B43

The runtime sources listed by upstream `RNNOISE_SOURCES`, their required
headers, and the full generated model are vendored here. `src/os_support.h`
is a build compatibility shim for the single `OPUS_CLEAR` macro used by the
upstream vector headers.
