#!/usr/bin/perl

# Usage:
# eb-cli-setup.pl <application-name>

# This script sets up the python venv and AWS eb cli. The next step
# is 'eb init'.

# <application-name> can be a new application name or an existing one such
# as helodali-prod or helodali-test.

use strict;
use Term::ANSIColor;
use warnings FATAL => 'all';

# Unbuffer I/O
$| = 1;

if (!@ARGV) {
    printError("Must provide application-name as argument!\n");
    exit 1;
}
my $applicationName = $ARGV[0];

qx(mkdir python3-venv);
print qx(python3 -m venv python3-venv);
if ($? != 0) {
    printError("Unable to execute python3 -m venv ...: $!\n");
    exit 1;
};

qx(. python3-venv/bin/activate);
qx(pip install awsebcli --upgrade);
#
# Make sure the AWS eb cli is installed
qx(eb --version) or die "Must install aws eb cli, see README.MD";

$ENV{AWS_PROFILE} = "default";

# Deploy the project and let EB build and run the docker container
print("Now execute: eb init $applicationName --platform docker-18.06.1-ce --region us-east-1 --interactive\n");


sub printWarning {
    my $msg = shift;
    print color('bold yellow'), $msg, color('reset');
}

sub printError {
    my $msg = shift;
    print color('bold red'), $msg, color('reset');
}
