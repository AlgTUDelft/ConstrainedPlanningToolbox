#################################################################################
# ConstrainedPlanningToolbox
# Copyright (C) 2019 Algorithmics group, Delft University of Technology
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#################################################################################

import random

class ProbabilitySample:

    def __init__(self):
        self.items = []
        self.probabilities = []

    def __init__(self, items, probabilities):
        self.items = items
        self.probabilities = probabilities

    def add_item(self, item, prob):
        self.items.append(item)
        self.probabilities.append(prob)

    def sample_item(self):
        if len(self.items) == 1:
            return self.items[0]
        else:
            random_number = random.random()
            cumulative = 0
            ret_item = -1

            for i in range(len(self.items)):
                cumulative += self.probabilities[i]

                if random_number <= cumulative:
                    ret_item = self.items[i]
                    break

            return ret_item
