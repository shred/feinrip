/bin/touch --no-create %{_datadir}/icons/hicolor &>/dev/null || :
/bin/sed --in-place -e "s:_datadir_:%{_datadir}:" %{_bindir}/feinrip