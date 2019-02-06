#!/usr/bin/perl

use warnings FATAL => 'all';
use strict;

$ENV{AWS_PROFILE} = "helodali";

my $prefix = "ribbon-maker";
my $version = "1.0.0";

my $KEY = "${prefix}-$$.zip";

print qx(aws s3 cp ${prefix}.zip s3://helodali-exports/$KEY);
print qx(aws lambda update-function-code --function-name ${prefix} --s3-bucket "helodali-exports" --s3-key $KEY);
print qx(aws s3 rm s3://helodali-exports/$KEY);
